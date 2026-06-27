import SockJS from 'sockjs-client'
import { Client as StompClient } from '@stomp/stompjs'

/**
 * Subscribes to live bid updates for one auction.
 * Server emits to: /topic/auctions/{auctionId}
 */
export function subscribeToAuction(auctionId, onUpdate) {
  const token = localStorage.getItem('token')

  const url = '/ws-chat'
  const socketFactory = () => new SockJS(url)

  const client = new StompClient({
    webSocketFactory: socketFactory,
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    reconnectDelay: 2000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    debug: () => {}
  })

  let sub = null

  client.onConnect = () => {
    sub = client.subscribe(`/topic/auctions/${auctionId}`, (msg) => {
      try {
        const payload = JSON.parse(msg.body)
        onUpdate && onUpdate(payload)
      } catch (e) {
        console.error('[auctionRealtime] bad message', e)
      }
    })
  }

  client.onStompError = (frame) => {
    console.error('[auctionRealtime] stomp error', frame?.headers, frame?.body)
  }

  client.activate()

  return {
    disconnect() {
      try { if (sub) sub.unsubscribe() } catch {}
      try { client.deactivate() } catch {}
      sub = null
    }
  }
}

