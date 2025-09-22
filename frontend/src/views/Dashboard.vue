<template>
  <div class="dashboard">
    <div class="page-header">
      <h1 class="title">仪表盘</h1>
      <p class="description">欢迎使用MMS制造管理系统</p>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="stat in stats" :key="stat.key">
        <div class="stat-card">
          <div class="stat-icon" :class="stat.color">
            <el-icon :size="24">
              <component :is="stat.icon" />
            </el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="card-header">
            <h3>合同状态分布</h3>
          </div>
          <div class="chart-content">
            <v-chart :option="contractStatusChart" style="height: 300px;" />
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="card-header">
            <h3>月度合同趋势</h3>
          </div>
          <div class="chart-content">
            <v-chart :option="monthlyTrendChart" style="height: 300px;" />
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 最近活动 -->
    <div class="chart-card">
      <div class="card-header">
        <h3>最近活动</h3>
        <el-button type="primary" size="small">查看全部</el-button>
      </div>
      <div class="activity-content">
        <el-timeline>
          <el-timeline-item
            v-for="activity in recentActivities"
            :key="activity.id"
            :timestamp="activity.time"
            :type="activity.type"
          >
            <div class="activity-item">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-desc">{{ activity.description }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'

use([
  CanvasRenderer,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const stats = ref([
  {
    key: 'contracts',
    label: '总合同数',
    value: '156',
    icon: 'Document',
    color: 'blue'
  },
  {
    key: 'processing',
    label: '处理中',
    value: '23',
    icon: 'Loading',
    color: 'orange'
  },
  {
    key: 'completed',
    label: '已完成',
    value: '128',
    icon: 'CircleCheck',
    color: 'green'
  },
  {
    key: 'components',
    label: '零部件数',
    value: '2,456',
    icon: 'Box',
    color: 'purple'
  }
])

const contractStatusChart = ref({
  tooltip: {
    trigger: 'item'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '合同状态',
      type: 'pie',
      radius: '50%',
      data: [
        { value: 128, name: '已完成' },
        { value: 23, name: '处理中' },
        { value: 5, name: '草稿' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
})

const monthlyTrendChart = ref({
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      data: [12, 18, 25, 22, 28, 35],
      type: 'line',
      smooth: true,
      areaStyle: {}
    }
  ]
})

const recentActivities = ref([
  {
    id: 1,
    title: '合同CT-2024-001工艺分解完成',
    description: '合同号：CT-2024-001，项目：某大厦电梯项目',
    time: '2024-01-15 14:30',
    type: 'success'
  },
  {
    id: 2,
    title: '新增零部件：电梯门机',
    description: '零部件代号：DM-001，规格：1000kg',
    time: '2024-01-15 10:20',
    type: 'primary'
  },
  {
    id: 3,
    title: '合同CT-2024-002开始工艺分解',
    description: '合同号：CT-2024-002，项目：商业中心电梯项目',
    time: '2024-01-15 09:15',
    type: 'warning'
  },
  {
    id: 4,
    title: '装箱单上传成功',
    description: '合同号：CT-2024-003，装箱单：PACK-001',
    time: '2024-01-14 16:45',
    type: 'info'
  }
])

onMounted(() => {
  // 加载仪表盘数据
  loadDashboardData()
})

const loadDashboardData = async () => {
  try {
    // 这里可以调用API加载实际数据
    console.log('Loading dashboard data...')
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  
  .title {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }
  
  .description {
    color: #606266;
    font-size: 16px;
  }
}

.stats-row {
  margin-bottom: 30px;
  
  .stat-card {
    background: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      
      &.blue {
        background: #e6f7ff;
        color: #1890ff;
      }
      
      &.orange {
        background: #fff7e6;
        color: #fa8c16;
      }
      
      &.green {
        background: #f6ffed;
        color: #52c41a;
      }
      
      &.purple {
        background: #f9f0ff;
        color: #722ed1;
      }
    }
    
    .stat-content {
      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }
}

.charts-row {
  margin-bottom: 30px;
  
  .chart-card {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .card-header {
      padding: 20px 20px 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin: 0;
      }
    }
    
    .chart-content {
      padding: 20px;
    }
  }
}

.activity-content {
  padding: 20px;
  
  .activity-item {
    .activity-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .activity-desc {
      font-size: 12px;
      color: #909399;
    }
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 10px;
  }
  
  .stats-row {
    .stat-card {
      padding: 15px;
      
      .stat-icon {
        width: 50px;
        height: 50px;
        margin-right: 12px;
      }
      
      .stat-content {
        .stat-value {
          font-size: 20px;
        }
      }
    }
  }
}
</style>
