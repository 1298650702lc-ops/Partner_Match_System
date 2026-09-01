import Index from "../Pages/Index.vue";
import TeamPage from "../Pages/TeamPage.vue";
import UserPage from "../Pages/UserPage.vue";
import SearchPage from "../Pages/SearchPage.vue";
import UserEditPage from "../Pages/UserEditPage.vue";
import UserResultPage from "../Pages/SearchResultPage.vue";
import UserLoginPage from "../Pages/UserLoginPage.vue";
//定义一些路由
const routes = [
    { path: '/', component: Index },
    { path: '/team', component: TeamPage },
    { path: '/user', component: UserPage },
    { path: '/search', component: SearchPage },
    { path: '/user/edit', component: UserEditPage },
    { path: '/user/list', component: UserResultPage },
    { path: '/user/login', component: UserLoginPage },
]
export default routes;
