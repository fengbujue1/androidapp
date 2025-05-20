/**
 * 远程日志实现
 * 参考Java CustomAppender的功能，实现日志上报
 */

// 日志发送的目标URL
const LOG_URL = 'https://biexiaozhi.cn/log';

// 线程池大小（JavaScript中使用并发控制）
const MAX_CONCURRENT_REQUESTS = 4;
let pendingRequests = 0;
let requestQueue = [];

// 日志级别
const LOG_LEVELS = {
	DEBUG: 'DEBUG',
	INFO: 'INFO',
	WARN: 'WARN',
	ERROR: 'ERROR'
};

// 获取客户端标识
function getClientId() {
	try {
		const systemInfo = uni.getSystemInfoSync();
		if (systemInfo && systemInfo.deviceId) {
			return `zsh_${systemInfo.deviceId}`;
		}
	} catch (e) {
		// 获取失败时不做特殊处理
	}
	return 'zsh';
}

// 创建日志JSON数据
function createJsonLog(logType, message, stack) {
	// 从错误堆栈中提取文件名和行号
	let fileInfo = '';
	if (stack) {
		const stackLines = stack.split('\n');
		if (stackLines.length > 1) {
			const callerLine = stackLines[1].trim();
			const match = callerLine.match(/at\s+(.*)\s+\((.*):(\d+):(\d+)\)/) ||
				callerLine.match(/at\s+(.*):(\d+):(\d+)/);

			if (match) {
				const hasMethodName = match.length > 4;
				const fileName = hasMethodName ? match[2] : match[1];
				const lineNumber = hasMethodName ? match[3] : match[2];
				// fileInfo = `(${fileName}:${lineNumber})`;
			}
		}
	}

	// 创建日志对象
	const jsonObject = {
		clientId: getClientId(),
		logType: logType,
		timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19),
		threadId: 'main', // JavaScript是单线程的，可以使用"main"作为默认值
		message: fileInfo + message
	};

	return JSON.stringify(jsonObject);
}

// 发送日志到服务器
function postLogs(jsonData) {
	// 如果当前请求数达到最大，加入队列
	if (pendingRequests >= MAX_CONCURRENT_REQUESTS) {
		requestQueue.push(jsonData);
		return;
	}

	pendingRequests++;

	uni.request({
		url: LOG_URL,
		method: 'POST',
		data: jsonData,
		header: {
			'content-type': 'application/json'
		},
		complete: () => {
			pendingRequests--;
			// 如果队列中有等待的请求，继续处理
			if (requestQueue.length > 0) {
				const nextData = requestQueue.shift();
				postLogs(nextData);
			}
		}
	});
}

// 获取调用堆栈
function getStackTrace() {
	const obj = {};
	Error.captureStackTrace(obj, getStackTrace);
	return obj.stack;
}

// 远程日志记录器
const RemoteLogger = {
	debug(message) {
		const stack = getStackTrace();
		postLogs(createJsonLog(LOG_LEVELS.DEBUG, message, stack));
		// console.debug(message); // 保留控制台输出用于开发
	},

	info(message) {
		const stack = getStackTrace();
		postLogs(createJsonLog(LOG_LEVELS.INFO, message, stack));
		// console.info(message);
	},

	warn(message) {
		const stack = getStackTrace();
		postLogs(createJsonLog(LOG_LEVELS.WARN, message, stack));
		// console.warn(message);
	},

	error(message) {
		const stack = getStackTrace();
		postLogs(createJsonLog(LOG_LEVELS.ERROR, message, stack));
		// console.error(message);
	},

	// 覆盖原生console的日志方法
	replaceConsole() {
		const originalConsole = {
			log: console.log,
			debug: console.debug,
			info: console.info,
			warn: console.warn,
			error: console.error
		};

		// 替换控制台方法
		console.log = function(message) {
			RemoteLogger.info(message);
			// originalConsole.log.apply(console, arguments);
		};

		console.debug = function(message) {
		  RemoteLogger.debug(message);
		// originalConsole.debug.apply(console, arguments);
		};

		console.info = function(message) {
			RemoteLogger.info(message);
			// originalConsole.info.apply(console, arguments);
		};

		console.warn = function(message) {
			RemoteLogger.warn(message);
			// originalConsole.warn.apply(console, arguments);
		};

		console.error = function(message) {
			RemoteLogger.error(message);
			// originalConsole.error.apply(console, arguments);
		};
	}
};

export default RemoteLogger;