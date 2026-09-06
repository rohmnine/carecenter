/**
 * Session Storage Helper
 * 提供身份隔离的会话存储工具函数
 */

/**
 * 获取当前登录用户信息
 * @returns {Object|null} 用户信息对象
 */
export function getCurrentUser() {
    const currentIdentity = window.sessionStorage.getItem('currentIdentity');
    if (!currentIdentity) {
        return null;
    }
    
    const userKey = `user_${currentIdentity}`;
    const userStr = window.sessionStorage.getItem(userKey);
    return userStr ? JSON.parse(userStr) : null;
}

/**
 * 获取当前登录身份
 * @returns {string|null} 身份标识 (stu/admin/parent)
 */
export function getCurrentIdentity() {
    const currentIdentity = window.sessionStorage.getItem('currentIdentity');
    if (!currentIdentity) {
        return null;
    }
    
    const identityKey = `identity_${currentIdentity}`;
    const identityStr = window.sessionStorage.getItem(identityKey);
    return identityStr ? JSON.parse(identityStr) : null;
}

/**
 * 设置当前用户信息
 * @param {Object} user - 用户信息对象
 * @param {string} identity - 身份标识
 */
export function setCurrentUser(user, identity) {
    const userKey = `user_${identity}`;
    const identityKey = `identity_${identity}`;
    
    window.sessionStorage.setItem(userKey, JSON.stringify(user));
    window.sessionStorage.setItem(identityKey, JSON.stringify(identity));
    window.sessionStorage.setItem('currentIdentity', identity);
}

/**
 * 更新当前用户信息
 * @param {Object} user - 更新的用户信息
 */
export function updateCurrentUser(user) {
    const currentIdentity = window.sessionStorage.getItem('currentIdentity');
    if (!currentIdentity) {
        console.error('No current identity found');
        return;
    }
    
    const userKey = `user_${currentIdentity}`;
    window.sessionStorage.setItem(userKey, JSON.stringify(user));
}

/**
 * 清除当前用户会话
 */
export function clearCurrentSession() {
    const currentIdentity = window.sessionStorage.getItem('currentIdentity');
    if (currentIdentity) {
        const userKey = `user_${currentIdentity}`;
        const identityKey = `identity_${currentIdentity}`;
        
        window.sessionStorage.removeItem(userKey);
        window.sessionStorage.removeItem(identityKey);
    }
    window.sessionStorage.removeItem('currentIdentity');
    
    // 清除旧的键（向后兼容）
    window.sessionStorage.removeItem('user');
    window.sessionStorage.removeItem('identity');
}

/**
 * 检查是否已登录
 * @returns {boolean}
 */
export function isLoggedIn() {
    return getCurrentUser() !== null;
}