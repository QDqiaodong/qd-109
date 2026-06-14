import { reactive } from 'vue'

const state = reactive({
  shouldRefreshList: false,
  lastPostType: null,
  lastCategoryId: null
})

export function usePostStore() {
  const markListDirty = (postType, categoryId) => {
    state.shouldRefreshList = true
    state.lastPostType = postType
    state.lastCategoryId = categoryId
  }

  const consumeRefreshFlag = () => {
    const flag = state.shouldRefreshList
    state.shouldRefreshList = false
    return flag
  }

  const getAndClearLastContext = () => {
    const ctx = {
      postType: state.lastPostType,
      categoryId: state.lastCategoryId
    }
    state.lastPostType = null
    state.lastCategoryId = null
    return ctx
  }

  return {
    state,
    markListDirty,
    consumeRefreshFlag,
    getAndClearLastContext
  }
}
