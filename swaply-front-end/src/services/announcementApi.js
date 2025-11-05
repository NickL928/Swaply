import axios from 'axios'

const client = axios.create({ baseURL: '/api/announcements' })

export default {
  list() { return client.get('') },
  get(id) { return client.get(`/${id}`) },
}

