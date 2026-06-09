export const highlightKeyword = (text, keyword) => {
  if (!text || !keyword) return text
  const regex = new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<em class="highlight">$1</em>')
}

export const getContentSnippet = (content, keyword, snippetLength = 120) => {
  if (!content) return ''
  if (!keyword) return content.slice(0, snippetLength) + (content.length > snippetLength ? '...' : '')

  const lowerContent = content.toLowerCase()
  const lowerKeyword = keyword.toLowerCase()
  const index = lowerContent.indexOf(lowerKeyword)

  if (index === -1) {
    return content.slice(0, snippetLength) + (content.length > snippetLength ? '...' : '')
  }

  const halfLen = Math.floor(snippetLength / 2)
  let start = Math.max(0, index - halfLen)
  let end = Math.min(content.length, index + keyword.length + halfLen)

  if (start === 0) {
    end = Math.min(content.length, snippetLength)
  }
  if (end === content.length) {
    start = Math.max(0, content.length - snippetLength)
  }

  const prefix = start > 0 ? '...' : ''
  const suffix = end < content.length ? '...' : ''

  return prefix + content.slice(start, end) + suffix
}
