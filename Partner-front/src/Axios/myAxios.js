import axios from "axios";

// Set config defaults when creating the instance
const myAxios = axios.create({
    baseURL: '/api',
    withCredentials: true,
});

// 添加请求拦截器
myAxios.interceptors.request.use(function (config) {
    console.log("我要发送请求了,",config)
    return config;
}, function (error) {
    // 对请求错误做些什么
    return Promise.reject(error);
});

// 添加响应拦截器
myAxios.interceptors.response.use(function (response) {
    // 对响应数据做点什么
    console.log("我收到你的响应了,",response)
    const body = response.data;
    if (body && body.code === 40100) {
        // 后端 Session 失效时，清理前端缓存并统一回到登录页。
        sessionStorage.removeItem('partner-match.current-user');
        window.dispatchEvent(new CustomEvent('partner-match:unauthorized'));
        if (window.location.hash !== '#/user/login') {
            window.location.hash = '#/user/login';
        }
    }
    // 保留 Axios 响应对象，调用方统一从 response.data 读取后端业务响应。
    return response;
}, function (error) {
    // 对响应错误做点什么
    return Promise.reject(error);
});

export default myAxios;
