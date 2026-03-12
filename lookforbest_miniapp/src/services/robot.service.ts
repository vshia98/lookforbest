import { http } from './http'
import type { Robot, RobotFilter, RobotSummary, PagedData } from '../types/robot'

export const robotService = {
  /** 获取机器人列表 */
  list(filter: RobotFilter = {}) {
    return http.get<PagedData<RobotSummary>>('/robots', filter as Record<string, unknown>)
  },

  /** 获取机器人详情 */
  detail(id: number) {
    return http.get<Robot>(`/robots/${id}`)
  },

  /** 获取相似机器人 */
  similar(id: number, size = 6) {
    return http.get<RobotSummary[]>(`/robots/${id}/similar`, { size })
  },

  /** 机器人对比（最多4个） */
  compare(robotIds: number[]) {
    return http.post('/compare', { robotIds })
  },

  /** 搜索建议 */
  searchSuggest(q: string, size = 8) {
    if (!q) return Promise.resolve([])
    return http.get('/search/suggest', { q, size })
  },

  /** 获取分类 */
  categories() {
    return http.get('/categories')
  },
}
