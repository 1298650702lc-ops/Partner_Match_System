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

export const useUserStore = () => ({
  user,
  loading,
  isLoggedIn: computed(() => user.value !== null),
  setCurrentUser,
  clearCurrentUser,
  fetchCurrentUser,
})
