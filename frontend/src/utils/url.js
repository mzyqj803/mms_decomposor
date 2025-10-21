/**
 * URL工具函数
 */

/**
 * 将相对路径转换为完整的后端API URL
 * @param {string} relativePath - 相对路径，如 "/api/breakdown/merged/1/download/filename.pdf"
 * @returns {string} - 完整的URL，如 "http://localhost:8080/api/breakdown/merged/1/download/filename.pdf"
 */
export function convertToBackendUrl(relativePath) {
  // 获取当前页面的协议和主机
  const protocol = window.location.protocol
  const hostname = window.location.hostname
  
  // 后端端口号（可以根据环境配置）
  const backendPort = 8080
  
  // 构建完整的后端URL
  return `${protocol}//${hostname}:${backendPort}${relativePath}`
}

/**
 * 检查URL是否为相对路径
 * @param {string} url - 要检查的URL
 * @returns {boolean} - 是否为相对路径
 */
export function isRelativePath(url) {
  return url.startsWith('/') && !url.startsWith('//')
}




