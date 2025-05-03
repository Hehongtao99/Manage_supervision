import fs from 'fs';
import path from 'path';
import https from 'https';
import { fileURLToPath } from 'url';

// 获取当前文件的目录
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 创建models目录（如果不存在）
const modelsDir = path.join(__dirname, 'models');
if (!fs.existsSync(modelsDir)) {
  fs.mkdirSync(modelsDir, { recursive: true });
}

// 使用jsdelivr CDN链接
const modelFiles = [
  {
    url: 'https://cdn.jsdelivr.net/npm/@vladmandic/face-api@1/model/tiny_face_detector_model-weights_manifest.json',
    dest: path.join(modelsDir, 'tiny_face_detector_model-weights_manifest.json')
  },
  {
    url: 'https://cdn.jsdelivr.net/npm/@vladmandic/face-api@1/model/tiny_face_detector_model-shard1',
    dest: path.join(modelsDir, 'tiny_face_detector_model-shard1')
  }
];

// 下载文件的函数
function downloadFile(url, dest) {
  return new Promise((resolve, reject) => {
    const file = fs.createWriteStream(dest);
    
    https.get(url, (response) => {
      response.pipe(file);
      
      file.on('finish', () => {
        file.close();
        console.log(`下载完成: ${dest}`);
        resolve();
      });
    }).on('error', (err) => {
      fs.unlink(dest, () => {}); // 删除不完整的文件
      console.error(`下载失败: ${err.message}`);
      reject(err);
    });
  });
}

// 顺序下载所有文件
async function downloadAllModels() {
  try {
    for (const file of modelFiles) {
      console.log(`开始下载: ${file.url}`);
      await downloadFile(file.url, file.dest);
    }
    console.log('所有模型文件已成功下载！');
  } catch (error) {
    console.error('下载过程中发生错误:', error);
  }
}

downloadAllModels(); 