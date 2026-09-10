import { computed, ref } from 'vue'
import myAxios from '../Axios/myAxios'
import type { CurrentUser } from '../models/user'

const STORAGE_KEY = 'partner-match.current-user'

const restoreUser = (): CurrentUser | null => {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) as CurrentUser : null
  } catch {
    sessionStorage.removeItem(STORAGE_KEY)
    return null
  }
}

// Module-level refs form one shared store for every page in this SPA.
const user = ref<CurrentUser | null>(restoreUser())
const loading = ref(false)

const setCurrentUser = (nextUser: CurrentUser | null) => {
  user.value = nextUser
  if (nextUser) {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(nextUser))
  } else {
    sessionStorage.removeItem(STORAGE_KEY)
  }
}

const clearCurrentUser = () => setCurrentUser(null)

if (typeof window !== 'undefined') {
  window.addEventListener('partner-match:unauthorized', clearCurrentUser)
}

const fetchCurrentUser = async (force = false): Promise<CurrentUser | null> => {
  if (user.value && !force) {
    return user.value
  }

  if (loading.value) {
    return user.value
  }

  loading.value = true
  try {
    const response = await myAxios.get('/user/current')
    const body = response.data
    if (body.code === 0 && body.data) {
      setCurrentUser(body.data as CurrentUser)
      return user.value
    }
    clearCurrentUser()
    return null
  } catch (error) {
    console.error('获取当前用户失败', error)
    return null
  } finally {
    loading.value = false
  }
}

/**
 * 退出登录：调用后端 /user/logout 清理 Session，并清空前端缓存的用户信息。
 * 无论后端是否成功，前端都会清理本地状态，避免出现"后端已失效但前端仍显示已登录"的情况。
 */
const logout = async (): Promise<boolean> => {
  try {
    const response = await myAxios.post('/user/logout')
    const body = response.data
    return body.code === 0
  } catch (error) {
    console.error('退出登录请求失败', error)
    return false
  } finally {
    clearCurrentUser()
  }
}

export const useUserStore = () => ({
  user,
  loading,
  isLoggedIn: computed(() => user.value !== null),
  setCurrentUser,
  clearCurrentUser,
  fetchCurrentUser,
  logout,
})
