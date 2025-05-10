<template>
  <div class="chat-page-wrapper">
    <!-- 完全独立于BaseLayout的聊天页面 -->
    <div class="chat-page">
      <!-- 添加返回按钮 -->
      <div class="back-button" @click="goBack">
        <el-button type="text" :icon="ArrowLeft">返回</el-button>
      </div>

      <div class="chat-container">
        <div class="chat-sidebar">
          <div class="sidebar-header">
            <h2>消息</h2>
            <el-dropdown v-if="isSupervisor" @command="handleCommand">
              <el-button type="primary" size="small">
                新建聊天 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="showStudentList">选择学生</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-dropdown v-else @command="handleCommand">
              <el-button type="primary" size="small">
                新建聊天 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="showSupervisorList">选择导师</el-dropdown-item>
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

    <!-- 教师选择对话框 -->
    <el-dialog
      v-model="showSupervisorDialog"
      title="选择导师"
      width="500px"
    >
      <el-input
        v-model="supervisorSearchKeyword"
        placeholder="搜索导师"
        prefix-icon="Search"
        clearable
        @input="handleSupervisorSearch"
      />
      
      <el-table
        :data="filteredSupervisors"
        style="width: 100%; margin-top: 16px;"
        height="350px"
        v-loading="loadingSupervisors"
      >
        <el-table-column prop="name" label="姓名" width="180" />
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="startChatWithSupervisor(scope.row)">
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
import { useUserStore } from '../../stores/user';
import { useChatStore } from '../../stores/chat';
import ChatList from '../../components/chat/ChatList.vue';
import ChatWindow from '../../components/chat/ChatWindow.vue';
import { ArrowDown, Search, ArrowLeft } from '@element-plus/icons-vue';
import axios from '../../utils/axios';
import { getSupervisors } from '../../api/supervisor';
import { useRouter } from 'vue-router';

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

// 教师选择对话框
const showSupervisorDialog = ref(false);
const supervisorSearchKeyword = ref('');
const supervisors = ref<any[]>([]);
const filteredSupervisors = ref<any[]>([]);
const loadingSupervisors = ref(false);

// 检查当前用户是否是教师
const isSupervisor = computed(() => userStore.isSupervisor);

// 下拉菜单命令处理
const handleCommand = (command: string) => {
  if (command === 'showStudentList') {
    showStudentDialog.value = true;
    loadStudents();
  } else if (command === 'showSupervisorList') {
    showSupervisorDialog.value = true;
    loadSupervisors();
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

// 加载教师列表
const loadSupervisors = async () => {
  loadingSupervisors.value = true;
  
  try {
    const supervisorList = await getSupervisors();
    
    // 添加数据验证
    if (Array.isArray(supervisorList)) {
      supervisors.value = supervisorList.map(supervisor => ({
        ...supervisor,
        name: supervisor.name || supervisor.username || 'Unknown Name',
        department: supervisor.department || 'Unknown Department'
      }));
      filteredSupervisors.value = supervisors.value;
    } else {
      console.error('Supervisor data format error:', supervisorList);
      supervisors.value = [];
      filteredSupervisors.value = [];
    }
  } catch (error) {
    console.error('Failed to load supervisor list:', error);
    supervisors.value = [];
    filteredSupervisors.value = [];
  } finally {
    loadingSupervisors.value = false;
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

// 搜索教师
const handleSupervisorSearch = () => {
  const keyword = supervisorSearchKeyword.value.toLowerCase();
  
  if (!keyword) {
    filteredSupervisors.value = supervisors.value;
    return;
  }
  
  filteredSupervisors.value = supervisors.value.filter(supervisor => 
    (supervisor.name && supervisor.name.toLowerCase().includes(keyword))
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

// 开始与教师聊天
const startChatWithSupervisor = async (supervisor: any) => {
  if (!supervisor || !supervisor.id) {
    console.error('Supervisor data incomplete, cannot start chat:', supervisor);
    return;
  }
  
  try {
    const conversation = await chatStore.getOrCreateConversationWithUser(supervisor.id);
    activeConversationId.value = conversation.id;
    showSupervisorDialog.value = false;
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

// 返回上一页
const goBack = () => {
  router.back();
};

// 初始化
onMounted(async () => {
  // 初始化聊天服务
  await chatStore.initChat();
  
  // 加载会话列表
  await chatStore.loadConversations();
  
  // 检查URL中是否有会话ID参数
  const route = router.currentRoute.value;
  const conversationIdParam = route.query.conversationId;
  
  if (conversationIdParam) {
    // 如果URL中有会话ID，则选择该会话
    const conversationId = parseInt(conversationIdParam as string, 10);
    if (!isNaN(conversationId)) {
      activeConversationId.value = conversationId;
      
      // 检查该会话是否已加载到聊天存储中
      if (!chatStore.conversations.some(c => c.id === conversationId)) {
        // 如果会话不在列表中，尝试加载该特定会话
        try {
          await chatStore.loadConversationById(conversationId);
        } catch (error) {
          console.error('Failed to load conversation by ID:', error);
        }
      }
    }
  } else if (chatStore.conversations.length > 0) {
    // 如果没有指定会话ID且有会话，选择第一个会话
    activeConversationId.value = chatStore.conversations[0].id;
  }
});
</script>

<style scoped>
.chat-page-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  width: 100vw;
  height: 100vh;
}

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
  background-color: #f0f2f5;
}

.chat-container {
  display: flex;
  height: 100%;
  max-height: 100vh;
  overflow: hidden !important;
  flex: 1;
  width: 100%;
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

.back-button {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2000;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 5px 15px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
}

.back-button:hover {
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}
</style> 