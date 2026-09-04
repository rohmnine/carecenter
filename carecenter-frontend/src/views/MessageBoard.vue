<template>
  <div class="message-board">
    <h1>Message Board</h1>
    <div v-for="message in messages" :key="message.id">
      <p><strong>{{ message.user.username }}:</strong> {{ message.content }}</p>
    </div>
    <form @submit.prevent="sendMessage">
      <input type="text" v-model="newMessage" placeholder="Type your message...">
      <button type="submit">Send</button>
    </form>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'MessageBoard',
  data() {
    return {
      messages: [],
      newMessage: ''
    }
  },
  async created() {
    try {
      const response = await api.getMessages();
      this.messages = response.data;
    } catch (error) {
      console.error(error);
    }
  },
  methods: {
    async sendMessage() {
      try {
        const response = await api.createMessage({
          content: this.newMessage,
          // In a real app, you'd get the user from the auth state
          user: { id: 1 } 
        });
        this.messages.push(response.data);
        this.newMessage = '';
      } catch (error) {
        console.error(error);
      }
    }
  }
}
</script>