import http from '@/services/api'

// 前端会话ID（本次会话唯一，刷新后不变）
function getSessionId(): string {
  let sid = sessionStorage.getItem('_sid')
  if (!sid) {
    sid = Math.random().toString(36).slice(2) + Date.now().toString(36)
    sessionStorage.setItem('_sid', sid)
  }
  return sid
}

type ActionType = 'view' | 'favorite' | 'compare' | 'search' | 'click' | 'share'
type TargetType = 'robot' | 'category' | 'manufacturer' | 'search'

interface TrackPayload {
  actionType: ActionType
  targetType: TargetType
  targetId?: number
  targetSlug?: string
  extraData?: Record<string, unknown>
}

/** 上报用户行为，失败静默忽略 */
export function track(payload: TrackPayload): void {
  http.post('/analytics/track', { sessionId: getSessionId(), ...payload }).catch(() => {})
}

/** 快捷方法 */
export const analytics = {
  viewRobot(id: number, slug: string) {
    track({ actionType: 'view', targetType: 'robot', targetId: id, targetSlug: slug })
  },
  searchRobot(keyword: string) {
    track({ actionType: 'search', targetType: 'search', extraData: { keyword } })
  },
  favoriteRobot(id: number) {
    track({ actionType: 'favorite', targetType: 'robot', targetId: id })
  },
  compareRobots(ids: number[]) {
    track({ actionType: 'compare', targetType: 'robot', extraData: { ids } })
  }
}
