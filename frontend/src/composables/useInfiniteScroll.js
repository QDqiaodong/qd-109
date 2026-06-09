import { ref, onMounted, onUnmounted, nextTick } from 'vue'

export function useInfiniteScroll(fetchFn, options = {}) {
  const {
    threshold = 200,
    pageSize = 10,
    immediate = true,
    useWindowScroll = true
  } = options

  const list = ref([])
  const loading = ref(false)
  const error = ref(null)
  const finished = ref(false)
  const pageNum = ref(1)
  const total = ref(0)
  const initialLoading = ref(false)

  const hasMore = () => {
    if (total.value === 0) return true
    return list.value.length < total.value
  }

  const loadMore = async () => {
    if (loading.value || finished.value) return

    const isFirstLoad = pageNum.value === 1 && list.value.length === 0
    if (isFirstLoad) {
      initialLoading.value = true
    }
    loading.value = true
    error.value = null

    try {
      const res = await fetchFn({
        pageNum: pageNum.value,
        pageSize
      })

      const records = res.records || []

      if (pageNum.value === 1) {
        list.value = records
      } else {
        list.value = [...list.value, ...records]
      }

      total.value = res.total || 0

      if (list.value.length >= total.value || records.length < pageSize) {
        finished.value = true
      } else {
        pageNum.value++
      }
    } catch (err) {
      error.value = err
    } finally {
      loading.value = false
      initialLoading.value = false
    }
  }

  const retry = () => {
    error.value = null
    loadMore()
  }

  const reset = () => {
    list.value = []
    pageNum.value = 1
    total.value = 0
    finished.value = false
    error.value = null
    loading.value = false
    initialLoading.value = false
  }

  const reload = () => {
    reset()
    nextTick(() => {
      loadMore()
    })
  }

  const getScrollBottom = () => {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop
    const scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight
    const clientHeight = document.documentElement.clientHeight || window.innerHeight
    return scrollHeight - scrollTop - clientHeight
  }

  const handleWindowScroll = () => {
    if (loading.value || finished.value || error.value) return

    const scrollBottom = getScrollBottom()

    if (scrollBottom <= threshold) {
      loadMore()
    }
  }

  let scrollContainer = null

  const handleContainerScroll = (e) => {
    if (loading.value || finished.value || error.value) return

    const target = e.target
    const scrollBottom = target.scrollHeight - target.scrollTop - target.clientHeight

    if (scrollBottom <= threshold) {
      loadMore()
    }
  }

  const setScrollContainer = (container) => {
    if (scrollContainer) {
      scrollContainer.removeEventListener('scroll', handleContainerScroll)
    }
    scrollContainer = container
    if (scrollContainer) {
      scrollContainer.addEventListener('scroll', handleContainerScroll)
    }
  }

  onMounted(() => {
    if (useWindowScroll) {
      window.addEventListener('scroll', handleWindowScroll)
    }
    if (immediate) {
      loadMore()
    }
  })

  onUnmounted(() => {
    if (useWindowScroll) {
      window.removeEventListener('scroll', handleWindowScroll)
    }
    if (scrollContainer) {
      scrollContainer.removeEventListener('scroll', handleContainerScroll)
    }
  })

  return {
    list,
    loading,
    initialLoading,
    error,
    finished,
    pageNum,
    total,
    loadMore,
    retry,
    reset,
    reload,
    setScrollContainer,
    hasMore
  }
}
