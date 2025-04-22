export interface TimetableDTO {
  id?: number;
  classId: number;
  className?: string;
  weekNumber: number;
  name: string;
  createTime?: string;
  updateTime?: string;
}

export interface TimetableItemDTO {
  id?: number;
  timetableId: number;
  teacherId?: number;
  teacherName?: string;
  courseName: string;
  dayOfWeek: number;
  startTime?: string;
  endTime?: string;
  periodType: 'morning' | 'afternoon' | 'evening';
  periodNumber: 1 | 2 | 3 | 4;
  classroom?: string;
}

export interface TimeSlot {
  startTime: string;
  endTime: string;
} 