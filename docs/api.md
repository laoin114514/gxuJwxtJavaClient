# 广西大学教务管理系统 API 文档

> 教务管理信息服务平台 (正方教务系统 v5 - ZFTAL UI)
>
> 基础 URL: `https://jwxt2018.gxu.edu.cn`
>
> 总计: **100+ 个端点** (38 JSON 数据 + 62 HTML 页面/其他)
>
> SDK: `python_sdk` 最后更新: 2026-05-17

---

## 目录

1. [认证流程 (5)](#1-认证流程)
2. [通用说明](#2-通用说明)
3. [成绩查询 (5)](#3-成绩查询)
4. [课表查询 (11)](#4-课表查询)
5. [考试安排 (3)](#5-考试安排)
6. [选课管理 (27)](#6-选课管理)
7. [学业与预警 (2)](#7-学业与预警)
8. [通知公告 (3)](#8-通知公告)
9. [待办事项 (1)](#9-待办事项)
10. [首页数据 (7 区域 + 4)](#10-首页数据)
11. [学生评价 (3)](#11-学生评价)
12. [教材管理 (3)](#12-教材管理)
13. [系统信息 (42)](#13-系统信息)
14. [端点汇总](#14-端点汇总)
15. [SDK 使用示例](#15-sdk-使用示例)

---

## 目录

1. [认证流程 (5)](#1-认证流程)
2. [通用说明](#2-通用说明)
3. [成绩查询 (3)](#3-成绩查询)
4. [课表查询 (6)](#4-课表查询)
5. [考试安排 (1)](#5-考试安排)
6. [选课管理 (27)](#6-选课管理)
7. [学业与预警 (2)](#7-学业与预警)
8. [通知公告 (3)](#8-通知公告)
9. [待办事项 (1)](#9-待办事项)
10. [首页数据 (7 区域 + 4)](#10-首页数据)
11. [学生评价 (3)](#11-学生评价)
12. [教材管理 (3)](#12-教材管理)
13. [系统信息 (26)](#13-系统信息)
14. [端点汇总](#14-端点汇总)
15. [SDK 使用示例](#15-sdk-使用示例)

---

## 1. 认证流程

登录使用 RSA 公钥加密密码，流程如下：

```
1. GET  /xtgl/login_slogin.html           → 获取 csrftoken, JSESSIONID
2. GET  /xtgl/login_getPublicKey.html     → 获取 RSA 公钥 {modulus, exponent}
3. RSA 加密密码 (PKCS#1 v1.5, base64输出)
4. POST /xtgl/login_slogin.html?time=...  → 提交登录表单 → 302 重定向
5. GET  /xtgl/index_initMenu.html?jsdm=xs → 初始化会话
```

> **SDK**: 以上流程由 `client.login()` 全自动完成。

### 1.1 获取登录页面

```
GET /jwglxt/xtgl/login_slogin.html
```

| 响应类型 | Content-Type |
|---------|-------------|
| HTML | text/html;charset=UTF-8 |

**返回**: 登录页面 HTML。从中提取:
- `<input name="csrftoken">` — CSRF 防护令牌
- `Set-Cookie: JSESSIONID=...` — 会话标识
- `Set-Cookie: route=...` — 路由标识

---

### 1.2 获取 RSA 公钥

```
GET /jwglxt/xtgl/login_getPublicKey.html?time={timestamp_ms}
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `time` | int | 毫秒时间戳，防缓存 |

**响应** (JSON):

```json
{
  "modulus": "AIg1wI0naCUS3nXWIivjRcjK54JuZ1XYHe0KrMEYFnIS4vj421ImAD8nXWf7o0LEs8...",
  "exponent": "AQAB"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `modulus` | string (base64) | RSA 公钥模数 n |
| `exponent` | string (base64) | RSA 公钥指数 e (=65537) |

**加密过程**:
```
n = int.from_bytes(base64.b64decode(modulus), 'big')
e = int.from_bytes(base64.b64decode(exponent), 'big')
密文 = PKCS1v15(UTF8(密码), RSAPublicKey(n, e))
输出 = base64.b64encode(密文)
```

---

### 1.3 提交登录

```
POST /jwglxt/xtgl/login_slogin.html?time={timestamp_ms}
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `csrftoken` | string | 是 | 从登录页面提取的 CSRF 令牌 |
| `language` | string | 是 | 固定 `zh_CN` |
| `yhm` | string | 是 | 学号 (用户名) |
| `mm` | string | 是 | RSA 加密 + base64 编码的密码 |
| `ydType` | string | 否 | 用户类型，留空 |

**成功**: 302 重定向到 `index_initMenu.html?jsdm=xs`  
**失败**: 200 停留在登录页面，HTML 中包含错误提示 (如 `#tips` 元素)

---

### 1.4 初始化首页会话

```
GET /jwglxt/xtgl/index_initMenu.html?jsdm=xs&_t={timestamp_ms}&echarts=1
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `jsdm` | string | 角色代码，学生为 `xs` |
| `_t` | int | 毫秒时间戳 |
| `echarts` | int | 是否加载图表组件，`1` |

---

### 1.5 退出登录

```
GET /jwglxt/xtgl/login_logoutAccount.html
```

---

## 2. 通用说明

### 2.1 请求头

所有请求需携带:

```
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9
Accept-Language: zh-CN,zh;q=0.9,en;q=0.8
Referer: https://jwxt2018.gxu.edu.cn/jwglxt/xtgl/index_initMenu.html
```

> **警告**: 不要声明 `br` (Brotli) 编码支持，`requests` 库无法解压。

### 2.2 分页查询参数 (PageQuery)

所有数据查询接口使用统一的 `application/x-www-form-urlencoded` 参数:

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `_search` | string | `false` | 是否全文搜索 |
| `nd` | int | 时间戳 | 随机数 |
| `queryModel.showCount` | int | `100` | 每页条数 (1-500) |
| `queryModel.currentPage` | int | `1` | 页码 (从1开始) |
| `queryModel.sortName` | string | `""` | 排序字段名 |
| `queryModel.sortOrder` | string | `"asc"` | 排序方向: `asc` / `desc` |
| `time` | int | 时间戳 | 防缓存 |

### 2.3 学期编码

| 编码 | 含义 |
|------|------|
| `3` | 第一学期 (9月 - 次年1月) |
| `12` | 第二学期 (2月 - 7月) |

学年使用起始年份: `"2025"` = 2025-2026学年。

### 2.4 响应格式

- **JSON 接口**: `Content-Type: application/json;charset=UTF-8`, 标准分页结构为 `{"items": [...], "totalResult": N}`
- **HTML 接口**: `Content-Type: text/html;charset=UTF-8`, 返回页面片段

### 2.5 错误处理

| 状态码 | 说明 |
|--------|------|
| 200 | 正常 |
| 302 | 重定向 (登录成功) |
| 404 | 接口不存在或当前角色不可用 |

---

## 3. 成绩查询

> SDK: `client.grades` — 5 个端点 (1 JSON + 4 HTML)

### 3.1 查询学期成绩

```
POST /jwglxt/cjcx/cjcx_cxDgXscj.html?doType=query&gnmkdm=N305005
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `xnm` | string | **是** | 学年，如 `"2025"` |
| `xqm` | string | **是** | 学期，`"3"` / `"12"` |
| `_search` | string | 否 | `"false"` |
| `nd` | int | 否 | 时间戳 |
| `queryModel.showCount` | int | 否 | 每页条数 |
| `queryModel.currentPage` | int | 否 | 页码 |
| `queryModel.sortName` | string | 否 | 排序字段 |
| `queryModel.sortOrder` | string | 否 | 排序方向 |
| `time` | int | 否 | 时间戳 |

**响应** (JSON):

```json
{
  "currentPage": 1,
  "totalResult": 13,
  "items": [
    {
      "kcmc": "数据结构",
      "cj": "85",
      "jd": "3.50",
      "xf": "4.5",
      "bfzcj": "85",
      "kcbj": "专业选修课",
      "kkxy": "计算机与电子信息学院",
      "kch": "1123456",
      "jxb_id": "394C1ED1F1DB529EE063020410AC1BDB",
      "cjbdsj": "2026-01-19 11:08:41",
      "cjsfzf": "否",
      "kssj": "2026-01-05(09:00-11:00)",
      "cxbj": "否"
    }
  ]
}
```

| 字段 (别名) | 类型 | 说明 |
|------|------|------|
| `kcmc` | string | 课程名称 |
| `cj` | string | 总评成绩 (数字) |
| `jd` | string | 绩点 |
| `xf` | string | 学分 |
| `bfzcj` | string | 百分制成绩 |
| `kcbj` | string | 课程标记 (必修/选修/通识必修等) |
| `kkxy` | string | 开课学院 |
| `kch` | string | 课程号 |
| `jxb_id` | string | 教学班ID (用于查询分项成绩) |
| `cjbdsj` | string | 成绩录入时间 |
| `cjsfzf` | string | 是否重修 |
| `kssj` | string | 考试时间 |

> **SDK**: `client.grades.query("2025", "3")` → `GradeQueryResult`

### 3.2 查询课程分项成绩

```
POST /jwglxt/cjcx/cjcx_cxDgXscj.html?doType=details&gnmkdm=N305005
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `jxb_id` | string | **是** | 教学班ID |
| `xh` | string | **是** | 学号 |

**响应**: HTML 页面，含该课程的平时/期中/期末等分项成绩表。

> **SDK**: `client.grades.detail(jxb_id)` → HTML string

### 3.3 成绩统计

```
GET /jwglxt/cjcx/cjcx_cxDgXscj.html?doType=statistics&gnmkdm=N305005&time={ts}
```

**响应**: HTML 页面，含总学分、平均绩点、各学期绩点趋势。

> **SDK**: `client.grades.statistics()` → HTML string

### 3.4 成绩导出

```
POST /jwglxt/cjcx/cjcx_dcListByXs.html?gnmkdm=N305005
Content-Type: application/x-www-form-urlencoded
```

**响应**: 导出文件 (HTML/Excel)。

> **SDK**: `client.grades.export()` → HTML string

---

## 4. 课表查询

> SDK: `client.schedule` — 11 个端点 (2 JSON + 9 HTML)

### 4.1 课表首页

```
GET /jwglxt/kbcx/xskbcx_cxXskbcxIndex.html?gnmkdm=N2151&layout=default&time={ts}
```

**响应**: HTML 页面 (约 25KB)，含本学期课表的表格数据。

> **SDK**: `client.schedule.page()` → HTML string

### 4.2 个人课表数据

```
POST /jwglxt/kbcx/xskbcx_cxXsgrkb.html?gnmkdm=N2151
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `xnm` | string | **是** | 学年 |
| `xqm` | string | **是** | 学期 |

**响应** (JSON):

```json
{
  "items": [
    {
      "kcmc": "软件工程",
      "jsxm": "陈老师",
      "sksj": "1-17周(7-8节)",
      "skxq": "星期四",
      "skdd": "6A-411",
      "jxb_id": "..."
    }
  ],
  "totalResult": 8
}
```

> **注意**: 需先加载课表首页 (`schedule.page()`) 初始化上下文，否则返回空列表。
>
> **SDK**: `client.schedule.personal("2025", "12")` → dict

### 4.3 教师课表

```
POST /jwglxt/kbcx/jskbcx_cxJsKb.html?gnmkdm=N2151
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `xnm` | string | **是** | 学年 |
| `xqm` | string | **是** | 学期 |
| `jsmc` | string | 否 | 教师姓名 |

**响应** (JSON): 含教师课表数据。

> **SDK**: `client.schedule.teacher("2025", "12", "教师名")` → dict

### 4.4 教室课表

```
POST /jwglxt/kbcx/jskbcx_cxJskb.html?gnmkdm=N2151
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `xnm` | string | **是** | 学年 |
| `xqm` | string | **是** | 学期 |
| `jsmc` | string | 否 | 教室名称 |

**响应**: HTML 页面。

> **SDK**: `client.schedule.classroom("2025", "12", "教室名")` → HTML string

### 4.5 班级课表

```
POST /jwglxt/kbcx/xskbcx_cxBjKb.html?gnmkdm=N253501
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `xnm` | string | **是** | 学年 |
| `xqm` | string | **是** | 学期 |

**响应**: HTML 页面。

> **SDK**: `client.schedule.class_group("2025", "12")` → HTML string

### 4.6 课表/学分确认

```
GET /jwglxt/kbcx/xskbqr_cxXskbqrIndex.html?gnmkdm=N2158&time={ts}
```

**响应**: HTML 页面。

> **SDK**: `client.schedule.credit_confirm()` → HTML string

### 4.7 课表导出 — 表格

```
POST /jwglxt/kbcx/xskbcx_cxDcExcelXskb.html?doType=table&gnmkdm=N2151
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `xnm` | string | 否 | 学年 |
| `xqm` | string | 否 | 学期 |

**响应**: 导出文件 (HTML/Excel)。

> **SDK**: `client.schedule.export_table(xnm, xqm)` → HTML string

### 4.8 课表导出 — 列表

```
POST /jwglxt/kbcx/xskbcx_cxDcExcelXskblb.html?doType=list&gnmkdm=N2151
Content-Type: application/x-www-form-urlencoded
```

参数同 4.7。

> **SDK**: `client.schedule.export_list(xnm, xqm)` → HTML string

### 4.9 课表简洁版

```
GET /jwglxt/kbcx/xskbcx_cxXskbSimpleIndex.html?gnmkdm=N2151
```

> **SDK**: `client.schedule.simple_view()` → HTML string

### 4.10 课表确认详情查询

```
POST /jwglxt/kbcx/xskbqr_cxXskbqrIndex.html?doType=query&gnmkdm=N2158
Content-Type: application/x-www-form-urlencoded
```

> **SDK**: `client.schedule.credit_confirm_detail()` → HTML string

### 4.11 课表确认提交

```
POST /jwglxt/kbcx/xskbqr_qrXskbqr.html?gnmkdm=N2158
Content-Type: application/x-www-form-urlencoded
```

> **SDK**: `client.schedule.credit_confirm_submit()` → HTML string

---

## 5. 考试安排

> SDK: `client.exams` — 3 个端点 (1 JSON + 2 HTML)

### 5.1 查询考试安排

```
POST /jwglxt/kwgl/kscx_cxXsksxxIndex.html?doType=query&gnmkdm=N3580
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `xnm` | string | 否 | 学年，不传查询全部 |
| `xqm` | string | 否 | 学期，不传查询全部 |

**响应** (JSON):

```json
{
  "currentPage": 1,
  "totalResult": 1,
  "items": [
    {
      "kcmc": "毛泽东思想和中国特色社会主义理论体系概论",
      "kssj": "2026-06-09(15:00-17:00)",
      "cdmc": "6A-405",
      "cdbh": "6A-405",
      "ksmc": "2025-2026学年第二学期本科课程期末考试",
      "khfs": "集中"
    }
  ]
}
```

| 字段 (别名) | 类型 | 说明 |
|------|------|------|
| `kcmc` | string | 课程名称 |
| `kssj` | string | 考试时间 (格式: `YYYY-MM-DD(HH:MM-HH:MM)`) |
| `cdmc` | string | 考场地点 |
| `cdbh` | string | 考场编号 |
| `ksmc` | string | 考试名称 |
| `khfs` | string | 考核方式 (集中/分散) |

> **SDK**: `client.exams.query("2025", "12")` → `ExamQueryResult`

### 5.2 导出考试安排

```
POST /jwglxt/kwgl/kscx_dcXsksxxList.html?gnmkdm=N358105
Content-Type: application/x-www-form-urlencoded
```

**响应**: 导出文件。

> **SDK**: `client.exams.export()` → HTML string

### 5.3 无排考课程列表

```
POST /jwglxt/kwgl/kscx_cxWpkskcList.html?doType=query&gnmkdm=N358105
Content-Type: application/x-www-form-urlencoded
```

**响应**: HTML 页面。

> **SDK**: `client.exams.unarranged_courses()` → HTML string

---

## 6. 选课管理

> SDK: `client.courses`
>
> **注意**: 选课数据查询受选课时间窗口限制，非选课期返回空列表或错误页面。

### 6.1 选课首页

```
GET /jwglxt/xsxk/zzxkyzb_cxZzxkYzbIndex.html?gnmkdm=N253512&layout=default
```

**响应**: HTML 页面 (约 21KB)。

> **SDK**: `client.courses.index_page()` → HTML string

---

### 6.2 课程列表展示 (主查询)

```
POST /jwglxt/xsxk/zzxkyzb_cxZzxkYzbDisplay.html
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `xkkz_id` | string | **是** | 选课控制ID |
| `kklxdm` | string | **是** | 选课类型代码 |
| `njdm_id` | string | 否 | 年级代码 |
| `zyh_id` | string | 否 | 专业代码 |
| `xszxzt` | string | 否 | 学生在线状态 |
| `kspage` | int | 否 | 起始页 (默认0) |
| `jspage` | int | 否 | 结束页 (默认0) |

**响应** (JSON):

```json
{
  "tmpList": [
    {
      "kch": "1123456",
      "kcmc": "数据库原理",
      "xf": "3.0",
      "cxbj": "0",
      "fxbj": "0",
      "xxkbj": "0",
      "sftj": "1",
      "kclxmc": "专业选修课",
      "kzmc": "数据库课程组",
      "jxb_id": "...",
      "jxbmc": "数据库原理-001",
      "jxbzrl": "60",
      "yxzrs": "45",
      "kcrow": "1"
    }
  ]
}
```

| 字段 (别名) | 类型 | 说明 |
|------|------|------|
| `kch` | string | 课程号 |
| `kcmc` | string | 课程名称 |
| `xf` | string | 学分 |
| `cxbj` | string | 是否重修 (`0`/`1`) |
| `fxbj` | string | 是否辅修 (`0`/`1`) |
| `xxkbj` | string | 是否限制选课 |
| `sftj` | string | 是否推荐课程 |
| `kclxmc` | string | 课程类型名称 |
| `kzmc` | string | 课程组名称 |
| `jxb_id` | string | 教学班ID |
| `jxbmc` | string | 教学班名称 |
| `jxbzrl` | string | 教学班容量 |
| `yxzrs` | string | 已选人数 |
| `kcrow` | string | 课程行号 |

> **SDK**: `client.courses.display(xkkz_id, kklxdm)` → `list[CourseItem]`

---

### 6.3 课程搜索 (分页/筛选)

```
POST /jwglxt/xsxk/zzxkyzb_cxZzxkYzbPartDisplay.html
Content-Type: application/x-www-form-urlencoded
```

**核心参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `xkkz_id` | string | **是** | 选课控制ID |
| `kklxdm` | string | **是** | 选课类型代码 |
| `xkxnm` | string | **是** | 选课学年 |
| `xkxqm` | string | **是** | 选课学期 |
| `njdm_id` | string | 否 | 年级代码 |
| `zyh_id` | string | 否 | 专业代码 |
| `kspage` | int | 否 | 起始页码 (从1开始) |
| `jspage` | int | 否 | 结束页码 |

**筛选参数** (均为可选):

| 参数 | 说明 |
|------|------|
| `xklc` | 选课轮次 |
| `xkly` | 选课来源 |
| `bklx_id` | 板块类型ID |
| `xqh_id` | 校区ID |
| `jg_id` | 学院ID |
| `zyfx_id` | 专业方向ID |
| `bh_id` | 班级ID |
| `xbm` | 性别 |
| `xslbdm` | 学生类别代码 |
| `xz` | 学制 |
| `ccdm` | 层次代码 |
| `sfkknj` | 是否开设年级 |
| `sfkkzy` | 是否开设专业 |
| `kzybkxy` | 可选专业必修 |
| `sfznkx` | 是否智能可选 |
| `zdkxms` | 指定可选模式 |
| `sfkxq` | 是否可选校区 |
| `sfkcfx` | 是否可跨校区 |
| `kkbk` | 开课板块 |
| `kkbkdj` | 开课板块等级 |
| `bklbkcj` | 板块类别课程级 |
| `sfkgbcx` | 是否可公选查询 |
| `sfrxtgkcxd` | 是否通识课 |
| `tykczgxdcs` | 体育课程最高选课次数 |
| `gnjkxdnj` | 高年级可选的年级 |
| `bbhzxjxb` | 包含合作教学班 |
| `kzkcgs` | 课程归属 |
| `rwlx` | 任务类型 |
| `rlkz` | 容量控制 |
| `xkzgbj` | 选课资格标记 |

**响应**: 同 [6.2 课程列表展示](#62-课程列表展示主查询) 格式。

> **SDK**: `client.courses.search(xkkz_id, kklxdm, xkxnm, xkxqm, **filters)` → `list[CourseItem]`

---

### 6.4 已选课程

```
POST /jwglxt/xsxk/zzxkyzb_cxZzxkYzbChoosed.html
Content-Type: application/x-www-form-urlencoded
```

**参数**: 无 (空 POST body `{}`)

**响应** (JSON Array):

```json
[
  {
    "kcmc": "数据库原理",
    "jxb_id": "...",
    "xf": "3.0",
    "skjs": "张老师",
    "sksj": "1-17周(3-4节)",
    "skdd": "6A-320"
  }
]
```

> **SDK**: `client.courses.selected()` → `list[dict]`

---

### 6.5 教学班详情

```
POST /jwglxt/xsxk/zzxkyzbjk_cxJxbWithKchZzxkYzb.html
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `kch_id` | string | **是** | 课程ID |
| `jxb_id` | string | **是** | 教学班ID |
| `xkkz_id` | string | **是** | 选课控制ID |
| `kklxdm` | string | **是** | 选课类型代码 |
| `xkxnm` | string | **是** | 学年 |
| `xkxqm` | string | **是** | 学期 |

另有 30+ 可选筛选参数 (同 6.3)。

**响应** (JSON Array):

```json
[
  {
    "jxb_id": "...",
    "bxbj": "1",
    "yxzrs": "45",
    "jxbrl": "60",
    "sksj": "1-17周(3-4节)",
    "ktmc": "多媒体教室",
    "jxdd": "6A-320",
    "jxms": "线下",
    "xf": "3.0",
    "skfsmc": "讲授",
    "kssj": "2026-01-10(09:00-11:00)",
    "kclbmc": "专业选修课",
    "kcxzmc": "考查",
    "xqumc": "东校区",
    "kkxymc": "计算机与电子信息学院",
    "jsxx": "10001/张老师/教授",
    "sfydjc": "0",
    "dsfrl": "5",
    "xkbz": ""
  }
]
```

| 字段 (别名) | 类型 | 说明 |
|------|------|------|
| `jxb_id` | string | 教学班ID |
| `bxbj` | string | 必修标记 |
| `yxzrs` | string | 已选人数 |
| `jxbrl` | string | 教学班容量 |
| `sksj` | string | 上课时间 |
| `ktmc` | string | 课堂名称 |
| `jxdd` | string | 教学地点 |
| `jxms` | string | 教学模式 |
| `xf` | string | 学分 |
| `skfsmc` | string | 授课方式 |
| `kssj` | string | 考试时间 |
| `kclbmc` | string | 课程类别 |
| `kcxzmc` | string | 课程性质 |
| `xqumc` | string | 校区名称 |
| `kkxymc` | string | 开课学院 |
| `jsxx` | string | 教师信息 (`工号/姓名/职称`) |
| `sfydjc` | string | 是否预定教材 |
| `dsfrl` | string | 待释放容量 |
| `xkbz` | string | 选课备注 |

> **SDK**: `client.courses.class_detail(kch_id, jxb_id, xkkz_id, kklxdm, xkxnm, xkxqm)` → `list[CourseClassDetail]`

---

### 6.6 一键选课

```
POST /jwglxt/xsxk/zzxkyzb_xkZzxkyzbQuickly.html
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `xkkz_id` | string | **是** | 选课控制ID |

**响应** (JSON):

```json
{
  "flag": "1",
  "msg": ""
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `flag` | string | `"0"` 失败, `"1"` 成功 |
| `msg` | string | 失败时的错误消息 |

> **SDK**: `client.courses.quick_select(xkkz_id)` → dict

---

### 6.7 学分检查

```
POST /jwglxt/xsxk/zzxkyzb_cxCheckZzxkyzbXfmcBynjzy.html
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `xnm` | string | **是** | 学年 |
| `xqm` | string | **是** | 学期 |
| `njdm_id` | string | **是** | 年级代码 |
| `zyh_id` | string | **是** | 专业代码 |
| `kklxdm` | string | **是** | 选课类型代码 |

**响应**: `"0"` (失败) 或 `"1~消息"` (通过)。

> **SDK**: `client.courses.check_credit(xnm, xqm, njdm_id, zyh_id, kklxdm)` → `{"result": "..."}`

---

### 6.8 学年学分验证

```
POST /jwglxt/xsxk/zzxkyzb_cxZzxkyzbLnyhxf.html
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `kklxdm` | string | **是** | 选课类型代码 |

**响应**: 纯数值 (总获得学分)。

> **SDK**: `client.courses.credit_validation(kklxdm)` → `{"credit": "..."}`

---

### 6.9-6.16 信息查看接口 (HTML)

| # | 接口 | 方法 | 参数 |
|---|------|------|------|
| 6.9 | `/xsxk/zzxkyzb_cxYinxyixxfView.html` | POST | `kklxdm` |
| 6.10 | `/xkgzsz/jbxkgzsz_cxJbxkgzsz.html` | POST | `xkkz_id`, `kklxdm` |
| 6.11 | `/xkgl/common_cxJsxxModel.html` | GET | `jgh_id`, `kch_id` |
| 6.12 | `/xkgl/common_cxKcxxModel.html` | GET | `kch_id` |
| 6.13 | `/xkgl/common_cxJxbrsmxIndex.html` | GET | `kch_id`, `jxb_id`, `xnm`, `xqm` |
| 6.14 | `/xsxk/tjxkyzb_cxJcxxList.html` | GET | `jxb_id` |
| 6.15 | `/xsxk/tjxkyzb_cxXkbzMsg.html` | GET | `jxb_id` |
| 6.16 | `/kbcx/xskbcx_cxXskbPopupIndex.html` | GET | `xnm`, `xqm` |

| 说明 | SDK 方法 |
|------|---------|
| 学分要求查看 | `client.courses.credit_requirement(kklxdm)` |
| 选课规则 | `client.courses.course_rules(xkkz_id, kklxdm)` |
| 教师简介弹窗 | `client.courses.teacher_info(jgh_id, kch_id)` |
| 课程简介弹窗 | `client.courses.course_info(kch_id)` |
| 教学班人数明细 | `client.courses.class_enrollment_detail(...)` |
| 教材信息 | `client.courses.textbook_info(jxb_id)` |
| 选课备注 | `client.courses.course_remark(jxb_id)` |
| 课表预览 | `client.courses.schedule_preview(xnm, xqm)` |

---

### 6.17 筛选条件查询 (12 个 GET JSON 接口)

以下接口返回下拉列表数据，用于高级搜索的筛选条件:

| # | 接口 | 说明 | SDK 方法 |
|---|------|------|---------|
| 1 | `GET /xkgl/common_queryKkbmPaged.html?localeKey=zh_CN` | 开课学院列表 | `filter_colleges()` |
| 2 | `GET /xkgl/common_queryNjPaged.html?njdm_id=` | 年级列表 | `filter_grades()` |
| 3 | `GET /xkgl/common_queryZyPaged.html?localeKey=zh_CN&jg_id=&zyh_id=` | 专业列表 | `filter_majors()` |
| 4 | `GET /xkgl/common_queryXquListPaged.html` | 校区列表 | `filter_campus()` |
| 5 | `GET /xkgl/common_queryKclbListPaged.html` | 课程类别列表 | `filter_course_types()` |
| 6 | `GET /xkgl/common_queryKcxzPaged.html` | 课程性质列表 | `filter_course_natures()` |
| 7 | `GET /xkgl/common_queryKcgsPaged.html` | 课程归属列表 | `filter_course_groups()` |
| 8 | `GET /xkgl/common_queryKczPaged.html` | 课程组列表 | `filter_course_clusters()` |
| 9 | `GET /jwglxt/xtgl/comm_cxJcsjList.html?lxdm=0032` | 教学模式列表 | `filter_teach_modes()` |
| 10 | `GET /jwglxt/xtgl/comm_cxJcsjList.html?lxdm=0036` | 上课星期列表 | `filter_weekdays()` |
| 11 | `GET /xkgl/common_querySkjcList.html` | 上课节次列表 | `filter_periods()` |
| 12 | `GET /xsxxxggl/xsxxwh_cxZyfxPaged.html` | 专业方向列表 | (未独立封装) |

**响应格式** (JSON Array):

```json
[
  {"dm": "01", "mc": "通识必修课"},
  {"dm": "02", "mc": "专业选修课"}
]
```

---

## 7. 学业与预警

> SDK: `client.academic`

### 7.1 学业情况

```
GET /jwglxt/xsxy/xsxyqk_cxXsxyqkIndex.html?gnmkdm=N105515&time={ts}
```

**响应**: HTML 页面 (约 1.6MB)，含已修学分、培养方案进度、各模块完成情况。

> **SDK**: `client.academic.status()` → HTML string

### 7.2 学籍预警

```
GET /jwglxt/xjyj/xjyj_cxXjyjIndex.html?gnmkdm=N105505&time={ts}
```

**响应**: HTML 页面。

> **SDK**: `client.academic.warning()` → HTML string

---

## 8. 通知公告

> SDK: `client.notifications`

### 8.1 通知列表

```
POST /jwglxt/xtgl/xwck_cxMoreXwList.html?doType=query
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `_search` | string | 否 | `"false"` |
| `nd` | int | 否 | 时间戳 |
| `queryModel.showCount` | int | 否 | 每页条数 (默认15) |
| `queryModel.currentPage` | int | 否 | 页码 |
| `queryModel.sortName` | string | 否 | `" "` (空格，按发布时间) |
| `queryModel.sortOrder` | string | 否 | `"desc"` |
| `time` | int | 否 | `0` |

**响应** (JSON):

```json
{
  "currentPage": 1,
  "totalResult": 14,
  "items": [
    {
      "xwbtqc": "（提醒）关于全校性通识选修线上课程的温馨提醒",
      "xwbt": "（提醒）关于全校性通识选修线上课程的温馨提醒",
      "fbsj": "2026-05-13",
      "xwbh": "51AF54A4CC628C3FE063020410AC12DD",
      "fbrxm": "",
      "sfzd": "1",
      "sfyd": "1"
    }
  ]
}
```

| 字段 (别名) | 类型 | 说明 |
|------|------|------|
| `xwbtqc` | string | 通知标题 (全称) |
| `xwbt` | string | 通知标题 (可能截断) |
| `fbsj` | string | 发布时间 (YYYY-MM-DD) |
| `xwbh` | string | 新闻编号 (用于获取详情) |
| `fbrxm` | string | 发布人姓名 |
| `sfzd` | string | 是否置顶 (`1`=是) |
| `sfyd` | string | 是否已读 (`1`=是) |

> **SDK**: `client.notifications.list()` → `NotificationQueryResult`

### 8.2 通知详情

```
GET /jwglxt/xtgl/xwck_ckXw.html?xwbh={新闻编号}&doType=save
```

**响应**: HTML 页面，含通知正文。

> **SDK**: `client.notifications.detail(news_id)` → HTML string

### 8.3 通知列表页

```
GET /jwglxt/xtgl/xwck_cxMoreXwList.html?doType=save
```

**响应**: HTML 页面。

---

## 9. 待办事项

> SDK: `client.notifications`

### 9.1 待办列表

```
POST /jwglxt/xtgl/index_cxDbsy.html?doType=query
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `flag` | string | 否 | `"1"` |
| `sfyy` | string | 否 | `"1"` |
| `queryModel.showCount` | int | 否 | 每页条数 (默认15) |
| `queryModel.sortName` | string | 否 | `"cjsj "` (按创建时间) |
| `queryModel.sortOrder` | string | 否 | `"desc"` |
| `time` | int | 否 | `0` |

**响应** (JSON):

```json
{
  "currentPage": 1,
  "totalResult": 23,
  "items": [
    {
      "xxbt": "调课提醒:张老师于第12-13周星期六第5-8节...",
      "xxnr": "调课提醒:张老师于第12-13周星期六第5-8节在大礼堂上的课程...",
      "cjsj": "2026-04-27 07:58:35",
      "clzt": "0",
      "yhm": "2407XXXXXX",
      "id": "506661FFF9BF6F9CE063030410ACE402"
    }
  ]
}
```

| 字段 (别名) | 类型 | 说明 |
|------|------|------|
| `xxbt` | string | 事项标题 |
| `xxnr` | string | 事项内容 |
| `cjsj` | string | 创建时间 (YYYY-MM-DD HH:MM:SS) |
| `clzt` | string | 处理状态 (`0`=未处理) |
| `yhm` | string | 用户学号 |
| `id` | string | 事项ID |

> **SDK**: `client.notifications.todos()` → `TodoQueryResult`

---

## 10. 首页数据

> SDK: `client.homepage`

### 10.1-10.7 首页区域

以下 7 个接口均为 `POST`，无请求体，返回 HTML 片段。

| # | 接口 | 说明 | SDK |
|---|------|------|-----|
| 10.1 | `POST /jwglxt/xtgl/index_cxAreaOne.html?localeKey=zh_CN&gnmkdm=index` | 本周课表 | `homepage.all()["schedule"]` |
| 10.2 | `POST /jwglxt/xtgl/index_cxAreaTwo.html?localeKey=zh_CN&gnmkdm=index` | 文件下载 | `homepage.all()["files"]` |
| 10.3 | `POST /jwglxt/xtgl/index_cxAreaThree.html?localeKey=zh_CN&gnmkdm=index` | 系统消息 | `homepage.all()["messages"]` |
| 10.4 | `POST /jwglxt/xtgl/index_cxAreaFour.html?localeKey=zh_CN&gnmkdm=index` | 成绩/考试 | `homepage.all()["grades_exams"]` |
| 10.5 | `POST /jwglxt/xtgl/index_cxAreaFive.html?localeKey=zh_CN&gnmkdm=index` | 校历 | `homepage.all()["calendar"]` |
| 10.6 | `POST /jwglxt/xtgl/index_cxAreaSix.html?localeKey=zh_CN&gnmkdm=index` | 校历文件 | `homepage.all()["calendar_files"]` |
| 10.7 | `POST /jwglxt/xtgl/index_cxNews.html?localeKey=zh_CN&gnmkdm=index` | 通知公告 | `homepage.all()["news"]` |

### 10.8 用户信息

```
POST /jwglxt/xtgl/index_cxYhxxIndex.html?xt=jw&localeKey=zh_CN&_={ts}&gnmkdm=index
```

**响应**: HTML 片段，含姓名、学号、院系、专业、班级、照片 `<img>` 标签。

> **SDK**: `client.homepage.user_info_html()` → HTML string

### 10.9 学生照片

```
GET /jwglxt/xtgl/photo_cxXszp4.html?xh_id={学号}&zplx=rxqzp
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `xh_id` | string | 学号 |
| `zplx` | string | 照片类型 (`rxqzp`=入学前照片) |

**响应**: 图片 (image/jpeg)。

> **SDK**: `client.homepage.photo_url(student_id)` → URL string

### 10.10 我的应用

```
POST /jwglxt/xtgl/index_cxWdyy.html?localeKey=zh_CN&gnmkdm=index&time={ts}
```

**响应** (JSON Array): 空数组或应用列表。

> **SDK**: `client.homepage.my_apps()` → list

### 10.11 最近使用

```
POST /jwglxt/xtgl/index_cxZjsy.html?localeKey=zh_CN&gnmkdm=index&time={ts}
```

**响应** (JSON Array): 空数组或最近使用的功能列表。

> **SDK**: `client.homepage.recently_used()` → list

---

## 11. 学生评价

> SDK: `client.evaluation`

| # | 接口 | 方法 | 说明 | SDK |
|---|------|------|------|-----|
| 11.1 | `/xspjgl/xspj_cxXspjIndex.html?gnmkdm=N401605&time={ts}` | GET | 学生评价 | `evaluation.student()` |
| 11.2 | `/jxbpjgl/ddpjkc_cxDdpjkcIndex.html?gnmkdm=N401637&time={ts}` | GET | 督导评价 | `evaluation.supervisor()` |
| 11.3 | `/jxbpjgl/ldpjkc_cxLdpjkcIndex.html?gnmkdm=N401642&time={ts}` | GET | 领导评价 | `evaluation.leader()` |

均返回 HTML 页面。

---

## 12. 教材管理

> SDK: `client.textbooks`

| # | 接口 | 方法 | 说明 | SDK |
|---|------|------|------|-----|
| 12.1 | `/jcydgl/xsjcyd_cxXsjcydIndex.html?gnmkdm=N253545&time={ts}` | GET | 教材预订 | `textbooks.booking()` |
| 12.2 | `/jcjsgl/xsjs_cxJcfyqrIndex.html?gnmkdm=N758066&time={ts}` | GET | 教材费用确认 | `textbooks.fee_confirm()` |
| 12.3 | `/jczdgl/jhjczdsq_cxJhjczdsqIndex.html?gnmkdm=N757010&time={ts}` | GET | 计划教材申请 | `textbooks.plan_apply()` |

均返回 HTML 页面。

---

## 13. 系统信息

> SDK: `client.system` — 42 个端点 (13 JSON + 14 计数/状态 + 15 HTML/其他)

### 13.0 菜单结构 (JSON)

```
GET /jwglxt/xtgl/index_cxMenuList.html
```

**响应** (JSON Array): 完整菜单树。每个节点含 `role` (menu/button)、`name` (名称)、`gnmkdm` (功能代码)、`url` (路径)、`children` (子节点)。

```json
[
  {
    "role": "menu",
    "name": "报名申请",
    "children": [
      {"role": "button", "name": "辅修报名", "gnmkdm": "N1053", "url": "/fxgl/fxbm_cxXsfxbmIndex.html"},
      {"role": "button", "name": "重修报名", "gnmkdm": "N1056", "url": "/cxbm/cxbm_cxXscxbmIndex.html"}
    ]
  }
]
```

> **SDK**: `client.system.menu_json()` → dict

### 13.1 预警与提示

| # | 接口 | 方法 | 说明 | SDK | 返回 |
|---|------|------|------|-----|------|
| 13.1.1 | `POST /jwglxt/xtgl/index_cxXttsxx.html?bj=2` | POST | 选修课毕业学分提示 | `tips()` | HTML |
| 13.1.2 | `POST /jwglxt/xtgl/index_cxKkyjxx.html` | POST | 旷课预警 | `absentee_warning()` | HTML |
| 13.1.3 | `POST /jwglxt/xtgl/index_cxLyyjxx.html` | POST | 劳育预警 | `labor_warning()` | HTML |

### 13.2 学业预警

| # | 接口 | 方法 | 说明 | SDK | 返回 |
|---|------|------|------|-----|------|
| 13.2.1 | `GET /jwglxt/xtgl/index_cxXsxyyjtxIndex.html` | GET | 学业预警列表 | `academic_warning_list()` | HTML |
| 13.2.2 | `POST /jwglxt/xtgl/index_cxXsxyyjtxView.html` | POST | 学业预警详情 | `academic_warning_detail()` | HTML |
| 13.2.3 | `POST /jwglxt/xtgl/index_cxXjyjqrzt.html` | POST | 学籍预警确认状态 | `academic_warning_confirm_status()` | HTML |

### 13.3 未提交计数 (JSON)

| # | 接口 | 方法 | 说明 | SDK | 返回类型 |
|---|------|------|------|-----|---------|
| 13.3.1 | `POST /jwglxt/xtgl/index_cxJhkcjcsqWtjNum.html` | POST | 教材申请未提交数 | `pending_textbook_apply()` | int |
| 13.3.2 | `POST /jwglxt/xtgl/index_cxXsGxkxfwxm.html` | POST | 公选课学分未修满 | `elective_credit_deficiency()` | dict `{flag}` |
| 13.3.3 | `POST /jwglxt/xtgl/index_cxXspjWtjNum.html` | POST | 学生评价未提交数 | `pending_student_evaluation()` | int |
| 13.3.4 | `POST /jwglxt/xtgl/index_cxThpjWtjNum.html` | POST | 同行评价未提交数 | `pending_peer_evaluation()` | int |
| 13.3.5 | `POST /jwglxt/xtgl/index_cxDdpjWtjNum.html` | POST | 督导评价未提交数 | `pending_supervisor_evaluation()` | int |
| 13.3.6 | `POST /jwglxt/xtgl/index_cxLdpjWtjNum.html` | POST | 领导评价未提交数 | `pending_leader_evaluation()` | int |

### 13.4 时间与状态 (JSON)

| # | 接口 | 方法 | 说明 | SDK | 返回类型 |
|---|------|------|------|-----|---------|
| 13.4.1 | `POST /jwglxt/xtgl/index_cxFxjftsNum.html` | POST | 辅修缴费提醒 | `minor_fee_reminder()` | dict |
| 13.4.2 | `POST /jwglxt/xtgl/index_cxUpdateFxjftszt.html` | POST | 更新辅修提醒状态 | `minor_fee_reminder_update()` | HTML |
| 13.4.3 | `POST /jwglxt/xtgl/index_cxBsxtsj.html` | POST | 毕设选题时间 | `thesis_topic_time()` | dict |
| 13.4.4 | `POST /jwglxt/xtgl/index_cxXfqrsj.html` | POST | 学分确认时间 | `credit_confirm_time()` | dict |
| 13.4.5 | `POST /jwglxt/xtgl/index_cxJssfqy.html` | POST | 角色是否启用 | `role_enabled()` | dict/int |
| 13.4.6 | `POST /jwglxt/xtgl/index_cxSzmrjs.html` | POST | 设置默认角色 | `set_default_role()` | dict |

### 13.5 监考信息

| # | 接口 | 方法 | 说明 | SDK | 返回 |
|---|------|------|------|-----|------|
| 13.5.1 | `POST /jwglxt/xtgl/index_cxJsjkxxList.html` | POST | 教师监考列表 | `exam_monitor_list()` | HTML |
| 13.5.2 | `POST /jwglxt/xtgl/index_cxJsjkxxView.html` | POST | 监考信息视图 | `exam_monitor_view()` | HTML |
| 13.5.3 | `POST /jwglxt/xtgl/index_cxKsjkxxList.html` | POST | 考试监考列表 (学生视角) | `exam_monitor_student_list()` | HTML |

### 13.6 其他

| # | 接口 | 方法 | 说明 | SDK | 返回 |
|---|------|------|------|-----|------|
| 13.6.1 | `POST /jwglxt/xtgl/index_cxXxdlztgx.html` | POST | 信息已读状态更新 | `mark_read()` | JSON |
| 13.6.2 | `POST /jwglxt/xtgl/index_yhqhAccount.html` | POST | 用户切换 | `switch_account()` | HTML |
| 13.6.3 | `GET /jwglxt/xtgl/index_cxKczywIndex.html` | GET | 可选业务 | `available_services()` | HTML |
| 13.6.4 | `GET /jwglxt/xtgl/index_cxGnjsView.html` | GET | 功能检索 | `feature_search()` | HTML |
| 13.6.5 | `GET /jwglxt/xtgl/index_cxGlwdyyView.html` | GET | 管理我的应用 | `manage_my_apps()` | HTML |
| 13.6.6 | `GET /jwglxt/xtgl/login_getYzm.html?time={ts}` | GET | 图片验证码 (旧版) | `captcha()` | bytes (image) |
| 13.6.7 | `GET /jwglxt/kaptcha` | GET | 图形验证码 (新版) | `kaptcha()` | bytes (image) |
| 13.6.8 | `GET /jwglxt/xtgl/init_cxBrowser.html` | GET | 浏览器检测 | `browser_check()` | HTML |
| 13.6.9 | `POST /jwglxt/xtgl/init_changeLocal.html` | POST | 切换语言 | `change_language()` | JSON |
| 13.6.10 | `GET /jwglxt/xtgl/mmgl_xgMm.html` | GET | 修改密码页面 | `change_password_page()` | HTML |
| 13.6.11 | `GET /jwglxt/xtgl/index_changeRole.html` | GET | 切换角色页面 | `change_role()` | HTML |
| 13.6.12 | `POST /jwglxt/xtgl/report_cxReportParams.html` | POST | 获取报表参数 | `report_params()` | HTML |
| 13.6.13 | `POST /jwglxt/xtgl/index_cxXsxyyjtx.html` | POST | 学业预警提醒 | `academic_warning_tx()` | HTML |
| 13.6.14 | `POST /jwglxt/xtgl/index_cxKkyjxxUpdate.html` | POST | 旷课预警状态更新 | `absentee_warning_update()` | HTML |
| 13.6.15 | `POST /jwglxt/xtgl/index_cxBczjsygnmk.html` | POST | 添加最近使用功能 | `add_recent_function()` | HTML |
| 13.6.16 | `GET /jwglxt/xtgl/file_cxDownFile.html` | GET | 文件下载 | `download_file()` | bytes |
| 13.6.17 | `GET /jwglxt/xtgl/file_cxViewFile.html` | GET | 文件查看 | `view_file()` | bytes |
| 13.6.18 | `GET /jwglxt/xtgl/dl_logout.html` | GET | 完全退出登录 | `logout_page()` | HTML |

---

## 14. 端点汇总

### 14.1 JSON 数据接口 (38 个)

| 模块 | 数量 | 端点 |
|------|------|------|
| 认证 | 1 | `login_getPublicKey` |
| 成绩 | 1 | `cjcx_cxDgXscj?doType=query` |
| 课表 | 2 | `xskbcx_cxXsgrkb`, `jskbcx_cxJsKb` |
| 考试 | 1 | `kscx_cxXsksxxIndex?doType=query` |
| 通知/待办 | 2 | `xwck_cxMoreXwList?doType=query`, `index_cxDbsy?doType=query` |
| 选课 | 16 | `display`, `search`, `selected`, `class_detail`, `quick_select`, `check_credit`, `credit_validation`, 12 个 filter |
| 首页 | 2 | `index_cxWdyy`, `index_cxZjsy` |
| 系统 | 13 | `menu_json`, 6 个未提交计数, 6 个时间/状态 |

### 14.2 HTML/其他接口 (62 个)

| 模块 | 数量 |
|------|------|
| 认证 | 4 |
| 成绩 | 4 |
| 课表 | 9 |
| 考试 | 2 |
| 选课 | 11 |
| 学业 | 2 |
| 通知 | 2 |
| 首页 | 8 |
| 评价 | 3 |
| 教材 | 3 |
| 系统 | 14 |

### 14.3 gnmkdm 编码对照表

| 编码 | 功能模块 |
|------|---------|
| `N305005` | 学生成绩查询 |
| `N2151` | 学生课表查询 |
| `N2158` | 课表/学分确认 |
| `N253501` | 班级课表 |
| `N253508` | 个人课表查询 (备选) |
| `N3580` | 考试信息查询 |
| `N358105` | 考试信息查询 (扩展) |
| `N253512` | 自主选课 |
| `N253511` | 学生选课 (推荐) |
| `N105515` | 学生学业情况 |
| `N105505` | 学籍预警 |
| `N305516` | 学业预警处理查询 |
| `N401605` | 学生评价 |
| `N401637` | 督导评价 |
| `N401642` | 领导评价 |
| `N401650` | 过程评价 |
| `N403050` | 课程问卷调查 |
| `N253545` | 教材预订 |
| `N758066` | 教材费用确认 |
| `N757010` | 计划教材申请 |
| `N353088` | 考试监考信息 |
| `N1598` | 网上上课地址 |
| `N2155` | 查询空闲教室 |
| `N255010` | 选课名单查询 |
| `N1053` | 辅修报名 |
| `N1056` | 重修报名 |
| `N2511` | 教学项目报名 |
| `N307010` | 成绩作废申请 |
| `N151530` | 校内课程替代申请 |
| `N104810` | 推免申请 |
| `N100801` | 查询个人信息 |
| `N100808` | 学生个人信息维护 |
| `N1532` | 个人培养方案 |
| `N153540` | 教学执行计划查看 |
| `N558020` | 学生成绩总表打印 |
| `N410510` | 教学信息反馈 |
| `N408130` | 最佳教师评选 |
| `N101508` | 学生注册 |
| `N100830` | 学生自主报到注册 |
| `N214505` | 班级课表查询 (打印) |

### 14.4 不可用 (返回 404)

| 接口 | 说明 |
|------|------|
| `GET /pyfa/*` (3个) | 培养方案模块不可用 (路径变更) |
| `GET /xsxxxggl/xsxxck_cxXsxxIndex.html` | 学籍信息不可用 |
| `POST /xjyd/xjyd_cxXjydIndex.html` | 学籍异动不可用 |
| `POST /jkap/ksjkxxcx_cxKsjkxxcxIndex.html` | 考试监考查询不可用 (学生角色) |

---

## 15. SDK 使用示例

```python
from python_sdk import JwxtClient, LoginError, PageQuery

client = JwxtClient("学号", "密码")

# 登录
try:
    client.login()
except LoginError as e:
    print(f"登录失败: {e}")
    exit(1)

# ---- 成绩 ----
grades = client.grades.query("2025", "3")
for item in grades.items:
    print(f"{item.course_name}: {item.score} (绩点 {item.grade_point})")

# ---- 考试 ----
exams = client.exams.query("2025", "12")
for item in exams.items:
    print(f"{item.course_name}: {item.exam_time} @ {item.location}")

# ---- 通知 ----
notifs = client.notifications.list()
for item in notifs.items:
    print(f"[{item.publish_date}] {item.title}")

# ---- 待办 ----
todos = client.notifications.todos()
for item in todos.items:
    print(f"[{item.create_time}] {item.title}")

# ---- 课表 ----
html = client.schedule.page()              # HTML 页面
data = client.schedule.personal("2025", "12")  # JSON 数据

# ---- 选课 (选课期间) ----
courses = client.courses.display(xkkz_id, kklxdm)
colleges = client.courses.filter_colleges()

# ---- 首页 ----
areas = client.homepage.all()
user = client.homepage.user_info_html()

# ---- 系统 ----
pending = client.system.pending_student_evaluation()
warnings = client.system.absentee_warning()
menu = client.system.menu_json()  # 完整菜单树 (JSON)

# ---- 课表 ----
client.schedule.export_table("2025", "12")     # 导出表格
client.schedule.simple_view()                   # 简洁版
client.schedule.credit_confirm_submit()          # 学分确认提交

# ---- 考试 ----
client.exams.export()           # 导出考试安排
client.exams.unarranged_courses()  # 无排考课程

# ---- 分页 ----
q = PageQuery(page=2, page_size=20)
grades = client.grades.query("2025", "3", query=q)

# ---- 通用查询 ----
result = client.query(
    "/jwglxt/cjcx/cjcx_cxDgXscj.html?doType=query&gnmkdm=N305005",
    {"xnm": "2025", "xqm": "3"}
)
html = client.query_page("/jwglxt/kbcx/xskbcx_cxXskbcxIndex.html?gnmkdm=N2151")

# ---- 学期推断 ----
semester = JwxtClient.current_semester()  # → Semester(year="2025", term="12")
```

---

## 依赖

```bash
pip install -r requirements.txt
# requests beautifulsoup4 cryptography pydantic
```

## 注意事项

1. 密码通过 RSA 公钥加密传输，不会明文发送
2. 请求间添加适当延迟，避免触发频率限制
3. 部分接口（如个人课表）需先加载对应页面初始化上下文
4. 选课数据查询受选课时间窗口限制
5. 不要在请求头声明 `br` (Brotli) 编码