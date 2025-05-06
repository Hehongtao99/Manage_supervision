<template>
  <div class="chat-page">
    <div class="chat-container">
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h2>消息</h2>
          <el-dropdown @command="handleCommand">
            <el-button type="primary" size="small">
              新建聊天 <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="isSupervisor" command="showStudentList">选择学生</el-dropdown-item>
                <el-dropdown-item command="showFriendList">选择好友</el-dropdown-item>
                <el-dropdown-item command="goToFriendsPage">管理好友</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <ChatList 
          :loading="loadingConversations"
          @select="handleSelectConversation"
        />
      </div>
      <div class="chat-main">
        <ChatWindow 
          :conversation-id="activeConversationId"
          @message-sent="handleMessageSent"
        />
      </div>
    </div>

    <!-- 学生选择对话框 -->
    <el-dialog
      v-model="showStudentDialog"
      title="选择学生"
      width="500px"
    >
      <el-input
        v-model="searchKeyword"
        placeholder="搜索学生"
        prefix-icon="Search"
        clearable
        @input="handleSearch"
      />
      
      <el-table
        :data="filteredStudents"
        style="width: 100%; margin-top: 16px;"
        height="350px"
        v-loading="loadingStudents"
      >
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="180" />
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="startChatWithStudent(scope.row)">
              开始聊天
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    
    <!-- 好友选择对话框 -->
    <el-dialog
      v-model="showFriendDialog"
      title="选择好友"
      width="500px"
    >
      <el-input
        v-model="friendSearchKeyword"
        placeholder="搜索好友"
        prefix-icon="Search"
        clearable
        @input="handleFriendSearch"
      />
      
      <el-table
        :data="filteredFriends"
        style="width: 100%; margin-top: 16px;"
        height="350px"
        v-loading="loadingFriends"
      >
        <el-table-column prop="name" label="姓名" width="180" />
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="startChatWithFriend(scope.row)">
              开始聊天
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user';
import { useChatStore } from '../../stores/chat';
import ChatList from '../../components/chat/ChatList.vue';
import ChatWindow from '../../components/chat/ChatWindow.vue';
import { ArrowDown, Search } from '@element-plus/icons-vue';
import axios from '../../utils/axios';

const router = useRouter();
const userStore = useUserStore();
const chatStore = useChatStore();

// 状态
const activeConversationId = ref<number | null>(null);
const loadingConversations = computed(() => chatStore.loadingConversations);

// 学生选择对话框
const showStudentDialog = ref(false);
const searchKeyword = ref('');
const students = ref<any[]>([]);
const filteredStudents = ref<any[]>([]);
const loadingStudents = ref(false);

// 好友选择对话框
const showFriendDialog = ref(false);
const friendSearchKeyword = ref('');
const friends = ref<any[]>([]);
const filteredFriends = ref<any[]>([]);
const loadingFriends = ref(false);

// 检查当前用户是否是教师
const isSupervisor = computed(() => userStore.isSupervisor);
// 检查当前用户是否是管理员
const isAdmin = computed(() => userStore.isAdmin);

// 下拉菜单命令处理
const handleCommand = (command: string) => {
  if (command === 'showStudentList') {
    showStudentDialog.value = true;
    loadStudents();
  } else if (command === 'showFriendList') {
    showFriendDialog.value = true;
    loadFriends();
  } else if (command === 'goToFriendsPage') {
    router.push('/friends');
  }
};

// 加载学生列表
const loadStudents = async () => {
  loadingStudents.value = true;
  
  try {
    const response = await axios.get('/api/supervisor/students');
    
    // 添加数据验证
    if (Array.isArray(response.data)) {
      students.value = response.data.map(student => ({
        ...student,
        name: student.name || 'Unknown Name',
        studentId: student.studentId || 'Unknown ID'
      }));
      filteredStudents.value = students.value;
    } else {
      console.error('Student data format error:', response.data);
      students.value = [];
      filteredStudents.value = [];
    }
  } catch (error) {
    console.error('Failed to load student list:', error);
    students.value = [];
    filteredStudents.value = [];
  } finally {
    loadingStudents.value = false;
  }
};

// 加载好友列表
const loadFriends = async () => {
  loadingFriends.value = true;
  
  try {
    const response = await axios.get('/api/friends');
    
    if (Array.isArray(response.data)) {
      friends.value = response.data;
      filteredFriends.value = response.data;
    } else {
      console.error('Friend data format error:', response.data);
      friends.value = [];
      filteredFriends.value = [];
    }
  } catch (error) {
    console.error('Failed to load friend list:', error);
    friends.value = [];
    filteredFriends.value = [];
  } finally {
    loadingFriends.value = false;
  }
};

// 搜索学生
const handleSearch = () => {
  const keyword = searchKeyword.value.toLowerCase();
  
  if (!keyword) {
    filteredStudents.value = students.value;
    return;
  }
  
  filteredStudents.value = students.value.filter(student => 
    (student.name && student.name.toLowerCase().includes(keyword)) ||
    (student.studentId && student.studentId.toLowerCase().includes(keyword))
  );
};

// 搜索好友
const handleFriendSearch = () => {
  const keyword = friendSearchKeyword.value.toLowerCase();
  
  if (!keyword) {
    filteredFriends.value = friends.value;
    return;
  }
  
  filteredFriends.value = friends.value.filter(friend => 
    (friend.name && friend.name.toLowerCase().includes(keyword)) ||
    (friend.username && friend.username.toLowerCase().includes(keyword))
  );
};

// 开始与学生聊天
const startChatWithStudent = async (student: any) => {
  if (!student || !student.id) {
    console.error('Student data incomplete, cannot start chat:', student);
    return;
  }
  
  try {
    const conversation = await chatStore.getOrCreateConversationWithUser(student.id);
    activeConversationId.value = conversation.id;
    showStudentDialog.value = false;
  } catch (error) {
    console.error('Failed to start chat:', error);
  }
};

// 开始与好友聊天
const startChatWithFriend = async (friend: any) => {
  if (!friend || !friend.id) {
    console.error('Friend data incomplete, cannot start chat:', friend);
    return;
  }
  
  try {
    const conversation = await chatStore.getOrCreateConversationWithUser(friend.id);
    activeConversationId.value = conversation.id;
    showFriendDialog.value = false;
  } catch (error) {
    console.error('Failed to start chat:', error);
  }
};

// 选择会话
const handleSelectConversation = (conversationId: number) => {
  activeConversationId.value = conversationId;
};

// 发送消息后处理
const handleMessageSent = () => {
  // 这里可以添加任何消息发送后的逻辑
};

// 初始化
onMounted(async () => {
  // 初始化聊天服务
  await chatStore.initChat();
  
  // 加载会话列表
  await chatStore.loadConversations();
  
  // 如果有会话，选择第一个
  if (chatStore.conversations.length > 0) {
    activeConversationId.value = chatStore.conversations[0].id;
  }
});
</script>

<style scoped>
.chat-page {
  height: 100vh;
  max-height: 100vh;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.chat-container {
  display: flex;
  height: 100%;
  max-height: 100vh;
  overflow: hidden !important;
  flex: 1;
}

.chat-sidebar {
  width: 300px;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.chat-main {
  flex: 1;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}
</style> 