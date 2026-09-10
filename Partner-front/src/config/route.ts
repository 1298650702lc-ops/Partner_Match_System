import Index from "../Pages/Index.vue";
import TeamPage from "../Pages/TeamPage.vue";
import UserPage from "../Pages/UserPage.vue";
import SearchPage from "../Pages/SearchPage.vue";
import UserEditPage from "../Pages/UserEditPage.vue";
import UserResultPage from "../Pages/SearchResultPage.vue";
import UserLoginPage from "../Pages/UserLoginPage.vue";
import TeamAddPage from "../Pages/TeamAddPage.vue";
import TeamEditPage from "../Pages/TeamEditPage.vue";
import UserTeamPage from "../Pages/UserTeamPage.vue";
//定义一些路由
const routes = [
    { path: '/', component: Index, meta: { requiresAuth: true } },
    { path: '/team', component: TeamPage, meta: { requiresAuth: true } },
    { path: '/user', component: UserPage, meta: { requiresAuth: true } },
    { path: '/search', component: SearchPage, meta: { requiresAuth: true } },
    { path: '/user/edit', component: UserEditPage, meta: { requiresAuth: true } },
    { path: '/user/list', component: UserResultPage, meta: { requiresAuth: true } },
    { path: '/user/login', component: UserLoginPage },
    { path: '/team/add', component: TeamAddPage, meta: { requiresAuth: true } },
    { path: '/team/edit', component: TeamEditPage, meta: { requiresAuth: true } },
    { path: '/user/team', component: UserTeamPage, meta: { requiresAuth: true } }
]
export default routes;
