// Mock data
let users = [
  {
    userId: '1',
    loginName: 'admin',
    username: '管理员',
    phoneNumber: '12345678901',
    status: 'Active',
    createTime: '2021-01-01 00:00:00',
    remark: '系统管理员'
  },
  // more users...
];

import { request } from '@/utils/utils'

const baseUrl = '/api/users';

export async function getMetadata() {
  return (await request({
    url: `${baseUrl}/metadata`,
    method: 'get',
  })).data.result;
}

// Mock API
export function getUsers() {
  return Promise.resolve({
    success: true,
    result: [...users]
  });
}

export function addUser(user) {
  user.userId = Date.now().toString(); // Generate a unique ID
  user.createTime = new Date().toISOString(); // Set the creation time
  users.push(user);
  return Promise.resolve({
    success: true,
    result: user
  });
}

export function updateUser(userId, user) {
  const index = users.findIndex(u => u.userId === userId);
  if (index !== -1) {
    users[index] = { ...users[index], ...user };
    return Promise.resolve({
      success: true,
      result: users[index]
    });
  } else {
    return Promise.resolve({
      success: false,
      msg: 'User not found'
    });
  }
}

export function deleteUser(userId) {
  const index = users.findIndex(u => u.userId === userId);
  if (index !== -1) {
    const deletedUser = users.splice(index, 1)[0];
    return Promise.resolve({
      success: true,
      result: deletedUser
    });
  } else {
    return Promise.resolve({
      success: false,
      msg: 'User not found'
    });
  }
}

export function searchUsers(userName) {
  const matchedUsers = users.filter(u => u.userName.includes(userName));
  return Promise.resolve({
    success: true,
    result: matchedUsers
  });
}
