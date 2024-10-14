package com.progartisan.module.bpm.model;

import io.leanddd.component.event.EntityUpdatedEvent;
import io.leanddd.module.bpm.model.UserSync;
import io.leanddd.module.user.api.Role;
import io.leanddd.module.user.api.User;
import io.leanddd.module.user.api.User.UserRole;
import org.flowable.engine.IdentityService;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.GroupQuery;
import org.flowable.idm.api.UserQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestUserSync {

    @Mock
    IdentityService identityService;

    @InjectMocks
    UserSync userSync;

    static Role role1;
    static Role role3;

    @Before
    public void init() {
        role1 = new Role();
        role1.setRoleType(Role.RoleType.GroupPublic);
        role1.setWorkflowGroup(true);
        role3 = new Role();
        role3.setRoleType(Role.RoleType.GroupPublic);
        role3.setWorkflowGroup(true);
    }

    @Test
    public void testSyncUserRole() {
        var groupQueryMock = mock(GroupQuery.class);
        Group mockGroup1 = mock(Group.class);
        when(mockGroup1.getId()).thenReturn("orgId-roleId1");
        Group mockGroup2 = mock(Group.class);
        when(mockGroup2.getId()).thenReturn("orgId-roleId2");
        var currentGroups = List.of(mockGroup1, mockGroup2);

        // 当调用 createGroupQuery 时返回 groupQueryMock
        when(identityService.createGroupQuery()).thenReturn(groupQueryMock);
        // 当调用 groupMember 和 list 方法时返回 expectedGroups
        when(groupQueryMock.groupMember("userId")).thenReturn(groupQueryMock);
        when(groupQueryMock.list()).thenReturn(currentGroups);

        var userId = "userId";
        var user = User.builder().userId(userId).build();
        user.setRoles(Set.of(UserRole.builder().userId(userId).roleId("roleId1").role(role1).orgId("orgId").build()));
        var userUpdatedEvent = new EntityUpdatedEvent(user);
        var userQueryMock = mock(UserQuery.class);
        when(identityService.createUserQuery()).thenReturn(userQueryMock);
        when(userQueryMock.userId("userId")).thenReturn(userQueryMock);
        when(userQueryMock.singleResult()).thenReturn(mock(org.flowable.idm.api.User.class));

        userSync.handleEntityUpdatedEvent(userUpdatedEvent);
        verify(identityService, times(0)).createMembership(any(), any());
        verify(identityService, times(1)).deleteMembership(userId, "orgId-roleId2");
    }

    @Test
    public void testSyncUserRole2() {
        var groupQueryMock = mock(GroupQuery.class);

        Group mockGroup1 = mock(Group.class);
        when(mockGroup1.getId()).thenReturn("orgId-roleId1");
        Group mockGroup2 = mock(Group.class);
        when(mockGroup2.getId()).thenReturn("orgId-roleId2");
        var currentGroups = List.of(mockGroup1, mockGroup2);
        // 当调用 createGroupQuery 时返回 groupQueryMock
        when(identityService.createGroupQuery()).thenReturn(groupQueryMock);
        // 当调用 groupMember 和 list 方法时返回 expectedGroups
        when(groupQueryMock.groupMember("userId")).thenReturn(groupQueryMock);
        when(groupQueryMock.list()).thenReturn(currentGroups);
        var userQueryMock = mock(UserQuery.class);
        when(identityService.createUserQuery()).thenReturn(userQueryMock);
        when(userQueryMock.userId("userId")).thenReturn(userQueryMock);
        when(userQueryMock.singleResult()).thenReturn(mock(org.flowable.idm.api.User.class));


        var userId = "userId";
        var user = User.builder().userId(userId).build();

        user.setRoles(Set.of(UserRole.builder().userId(userId).roleId("roleId1").role(role1).orgId("orgId").build(),
                UserRole.builder().userId(userId).roleId("roleId3").role(role3).orgId("orgId").build()));
        var userUpdatedEvent = new EntityUpdatedEvent(user);
        userSync.handleEntityUpdatedEvent(userUpdatedEvent);
        verify(identityService, times(1)).createMembership(userId, "orgId-roleId3");
        verify(identityService, times(1)).deleteMembership(userId, "orgId-roleId2");

    }
}
