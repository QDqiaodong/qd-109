import request from './request'

export const login = (data) => request.post('/users/login', data)
export const register = (data) => request.post('/users/register', data)

export const getCategories = () => request.get('/categories')

export const getPostList = (params) => request.get('/posts', { params })
export const getLatestPosts = () => request.get('/posts/latest')
export const getHotPosts = () => request.get('/posts/hot')
export const getPostDetail = (id) => request.get(`/posts/${id}`)
export const createPost = (data) => request.post('/posts', data)

export const searchPosts = (params) => request.get('/posts/search', { params })
export const getSearchSuggestions = (keyword) => request.get('/posts/search/suggestions', { params: { keyword } })

export const getComments = (postId) => request.get('/comments', { params: { postId } })
export const createComment = (data) => request.post('/comments', data)
