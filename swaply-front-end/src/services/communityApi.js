// Simple mock API for community threads
// Later you can replace with real HTTP calls using axios

function delay(ms) {
  return new Promise(res => setTimeout(res, ms))
}

const mockThreads = [
  {
    id: 't1',
    title: 'Welcome to Swaply Community 🎉',
    body: 'Introduce yourself and share what you are looking to swap! ',
    author: { id: 'u1', name: 'Admin' },
    createdAt: new Date().toISOString(),
    category: 'GENERAL',
    images: [],
    stats: { replies: 3, likes: 12, views: 120 },
    likedByMe: false
  },
  {
    id: 't2',
    title: 'Best practices for safe trading',
    body: 'Here are some tips to keep your trades safe and smooth...',
    author: { id: 'u2', name: 'Sarah' },
    createdAt: new Date(Date.now() - 86400000).toISOString(),
    category: 'GUIDE',
    images: [],
    stats: { replies: 5, likes: 25, views: 240 },
    likedByMe: true
  },
  {
    id: 't3',
    title: 'Looking for used textbooks',
    body: 'If you have CS or Math textbooks, please DM me or reply!',
    author: { id: 'u3', name: 'Leo' },
    createdAt: new Date(Date.now() - 2*86400000).toISOString(),
    category: 'REQUEST',
    images: [],
    stats: { replies: 2, likes: 4, views: 60 },
    likedByMe: false
  }
]

let threadIdCounter = 100

export default {
  async listThreads({ page = 1, size = 10, q, category, sort } = {}) {
    await delay(400)
    let items = [...mockThreads]
    if (q) {
      const s = q.toLowerCase()
      items = items.filter(t => t.title.toLowerCase().includes(s) || t.body.toLowerCase().includes(s))
    }
    if (category) {
      items = items.filter(t => t.category === category)
    }
    // naive sort: newest first
    if (!sort || sort === 'newest') {
      items.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    }
    const total = items.length
    const start = (page - 1) * size
    const pageItems = items.slice(start, start + size)
    return { items: pageItems, page, size, total }
  },
  async postThread({ title, body, category }) {
    await delay(300)
    const newThread = {
      id: 't' + (++threadIdCounter),
      title,
      body,
      author: { id: 'u0', name: 'You' },
      createdAt: new Date().toISOString(),
      category,
      images: [],
      stats: { replies: 0, likes: 0, views: 0 },
      likedByMe: false
    }
    mockThreads.unshift(newThread)
    return newThread
  }
}
