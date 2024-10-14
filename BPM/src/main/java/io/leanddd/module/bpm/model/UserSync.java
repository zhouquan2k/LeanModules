package io.leanddd.module.bpm.model;

import io.leanddd.component.common.Util;
import io.leanddd.component.event.EntityCreatedEvent;
import io.leanddd.component.event.EntityUpdatedEvent;
import io.leanddd.module.bpm.api.UserGroupService;
import io.leanddd.module.user.api.Role;
import io.leanddd.module.user.api.User.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.IdentityService;
import org.flowable.idm.api.User;
import org.springframework.context.event.EventListener;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class UserSync {
	private final IdentityService identityService;
	private final UserGroupService userGroupService;

	private String roleToGroupId(UserRole role) {
		if (role.getRole().getRoleType() == Role.RoleType.GroupPublic) {
			Util.check(Util.isNotEmpty(role.getOrgId()) && !Objects.equals(role.getRoleId(), "0"));
			return String.format("%s-%s", role.getOrgId(), role.getRoleId());
		} else {
			return role.getRoleId();
		}
	}

	private UserGroupService.Group roleToGroup(UserRole role) {
		if (role.getRole().getRoleType() == Role.RoleType.GroupPublic && role.getRole().getWorkflowGroup()) {
			return UserGroupService.Group.builder()
					.groupId(roleToGroupId(role))
					.groupName(role.getRoleName())
					.orgId(role.getOrgId())
					.roleId(role.getRoleId())
					.build();
		} else {
			return null;
		}
	}

	private void syncRoles(String userId, Set<UserRole> roles) {
		if (roles == null)
			roles = Set.of();
		var targetRoleMap = Util.toMap(
				roles.stream()
						.filter(role -> role.getRole()!=null && role.getRole().getWorkflowGroup() != null && role.getRole().getWorkflowGroup()),
				this::roleToGroupId);

		var groups = identityService.createGroupQuery().groupMember(userId).list();

		var alreadyExistSet = groups.stream().flatMap(group -> {
			String groupId = group.getId();
			// String roleId = groupId.substring(groupId.indexOf('-') + 1);
			if (!targetRoleMap.containsKey(groupId)) {
				log.debug(String.format("user %s remove from group: %s", userId, groupId));
				identityService.deleteMembership(userId, groupId);
				return Stream.of();
			} else {
				return Stream.of(groupId);
			}
		}).collect(Collectors.toSet());

		var rolesToAdd = roles.stream()
				.filter(role -> role.getRole() != null && role.getRole().getWorkflowGroup() != null && role.getRole().getWorkflowGroup()
						&& !alreadyExistSet.contains(roleToGroupId(role)));
		rolesToAdd.forEach(role -> {
			var group = roleToGroup(role);
			if (group != null) {
				if (!userGroupService.groupExists(group.getGroupId())) {
					userGroupService.createGroups(group);
				}
				log.debug(String.format("user %s join to group: %s", userId, group.getGroupId()));
				identityService.createMembership(userId, group.getGroupId());
			}
		});
	}

	void createUser(io.leanddd.module.user.api.User user) {
		var existingUser = identityService.createUserQuery().userId(user.getUserId()).singleResult();
		if (existingUser == null) {
			User fUser = identityService.newUser(user.getUserId());
			fUser.setFirstName(user.getUsername());

			identityService.saveUser(fUser);
			var roles = user.getRoles();
			syncRoles(user.getUserId(), roles);
		}
	}

	@EventListener
	public void handleEntityCreatedEvent(EntityCreatedEvent event) {
		log.info("> handle EntityCreatedEvent");
		if (event.getEntity() instanceof io.leanddd.module.user.api.User) {
			log.info("> user created");
			createUser((io.leanddd.module.user.api.User) event.getEntity());
		}
	}

	@EventListener
	public void handleEntityUpdatedEvent(EntityUpdatedEvent event) {
		log.info("> handle EntityUpdatedEvent");
		if (event.getEntity() instanceof io.leanddd.module.user.api.User) {
			log.info("> user updated");
			var user = (io.leanddd.module.user.api.User) event.getEntity();

			// in case the user not exist in workflow
			var existingUser = identityService.createUserQuery().userId(user.getUserId()).singleResult();
			if (existingUser == null) {
				User fUser = identityService.newUser(user.getUserId());
				fUser.setFirstName(user.getUsername());
				identityService.saveUser(fUser);
			}
			syncRoles(user.getUserId(), user.getRoles());
		}
	}
}
