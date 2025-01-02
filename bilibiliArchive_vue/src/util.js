import { ElMessageBox,ElMessage } from 'element-plus'

export function formatTimestamp(timestamp) {
  const date = new Date(timestamp);

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

export function formatTimestampYMD(timestamp) {
  const date = new Date(timestamp);

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
}

/**
 * Extracts the file name from a given URL.
 * @param {string} url - The URL to extract the file name from.
 * @returns {string|null} - The extracted file name, or null if none is found.
 */
export function getFileName(url) {
  try {
    const urlObj = new URL(url); // Parse the URL
    const pathname = urlObj.pathname; // Get the pathname
    return pathname.split('/').pop() || null; // Extract and return the file name
  } catch (error) {
    console.error('Invalid URL:', error);
    return null; // Return null if URL is invalid
  }
}

export function formatToQW(number) {
  if (typeof number !== 'number' || isNaN(number)) {
    throw new Error('Input must be a valid number');
  }

  if (number < 1000) {
    return number.toString();
  } else if (number < 10000) {
    return number.toString();
    // return (number / 1000).toFixed(1).replace(/\\.0$/, '') + '千';
  } else {
    return (number / 10000).toFixed(1).replace(/\\.0$/, '') + '万';
  }
}

export function getBCUserAvatarUrl(url) {
  return `/files/backup-config-avatars/user/${getFileName(url)}`;
}

export function getCommentAvatarUrl(url) {
  return `/api/comments/avatars/${getFileName(url)}`
}

export function getBCUploaderAvatarUrl(url) {
  return `/files/backup-config-avatars/uploader/${getFileName(url)}`;
}

export function closeDrawer() {
  new mdui.Drawer('#drawer').close();
}

export function ask(title, message, call) {
  ElMessageBox.alert(message, title, {
    showCancelButton: true,
    callback: action => {
      if (action == "confirm") {
        call();
      }
    }
  })
}

// 防止XSS的函数
export function escapeHtml(str) {
  return str.replace(/[&<>"']/g, (match) => {
    const escapeMap = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#039;',
    };
    return escapeMap[match] || match;
  });
}


export function formatDiskUsage(bytes) {
  if (bytes < 0) return "Invalid size";

  const units = ["B", "KB", "MB", "GB"];
  let index = 0;

  while (bytes >= 1024 && index < units.length - 1) {
    bytes /= 1024;
    index++;
  }

  return `${bytes.toFixed(2)} ${units[index]}`;
}

export function getVideoStateDesc(state) {
  switch (state) {
    case "NORMAL":
      return "正常"
    case "FAILED":
      return "失效"
    case "FAILED_UP_DELETE":
      return "UP主删除(被锁定后删除也为此状态)"
    case "PRIVATE":
      return "UP主设置仅自己可见"
    case "SHADOW_BAN":
      return "仅UP主可见(ShadowBan)"
    default:
      return state
  }
}

/**
 * 通用的确认对话框，需要用户输入指定文本以确认操作。
 * @param {string} title - 对话框标题
 * @param {string} message - 对话框文本
 * @param {string} confirmText - 用户需要输入的确认文本
 * @param {Function} onConfirm - 点击确定且输入匹配时执行的回调函数
 */
export function confirmWithInput(title, message, confirmText, onConfirm) {
  ElMessageBox.prompt(message, title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: `为防止手贱，请输入“${confirmText}”以确认`,
    inputValidator: (value) => value === confirmText || `请输入正确的确认文本：${confirmText}`,
  })
    .then(({ value }) => {
      if (value === confirmText) {
        onConfirm();
      } else {
        ElMessage.error('输入不匹配，请检查后重试');
      }
    })
    .catch(() => {
      // 用户点击取消
    });
}