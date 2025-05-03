export interface RegionData {
  id?: number;
  name: string;
  code: string;
  parentId?: number | null;
  parentName?: string;
  level?: number;
  sort?: number;
  status?: string;
  createTime?: string;
  updateTime?: string;
  children?: RegionData[];
}

export interface RegionTreeNode {
  id: number;
  label: string;
  code: string;
  level: number;
  children?: RegionTreeNode[];
} 