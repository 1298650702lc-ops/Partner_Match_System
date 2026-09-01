import { createApp } from 'vue'
import App from './App.vue'
import {Button, Icon, NavBar, Tabbar, TabbarItem} from 'vant';
import * as VueRouter from 'vue-router';
import routes from './config/route';

const app = createApp(App);
app.use(Button);
app.use(NavBar);
app.use(Icon);
app.use(Tabbar);
app.use(TabbarItem);


const router = VueRouter.createRouter({
    // 4. 内部提供了 history 模式的实现。为了简单起见，我们在这里使用 hash 模式。
    history: VueRouter.createWebHashHistory(),
    routes, // `routes: routes` 的缩写
})

router.beforeEach((to) => {
    const hasCachedUser = Boolean(sessionStorage.getItem('partner-match.current-user'));

    if (to.meta.requiresAuth && !hasCachedUser) {
        return {
            path: '/user/login',
            query: { redirect: to.fullPath },
        };
    }

    if (to.path === '/user/login' && hasCachedUser) {
        return '/';
    }
});

app.use(router)

app.mount('#app');
