# 🍕 OAuth2 Flows for Pizza App

This document describes two OAuth2 flows used in the Pizza App: **Client Credentials Flow** and **Authorization Code Flow**, along with a quick OAuth refresher.

---

## 🔑 What is OAuth?

OAuth (Open Authorization) is an open standard for token-based authorization. It allows applications to access resources on behalf of users without sharing their passwords.

---

## 🔄 OAuth 1.0 vs OAuth 2.0

| Feature                    | OAuth 1.0                             | OAuth 2.0                             |
|----------------------------|---------------------------------------|---------------------------------------|
| Signing                    | Requires request signing with secrets | Uses tokens for authorization        |
| Complexity                 | More complex, requires cryptographic signatures | Simpler, designed for modern apps     |
| Token Types                | Only access token                    | Access token and refresh token       |
| Mobile & Web Support       | Not designed for modern platforms    | Optimized for mobile/web platforms   |
| Security                   | Stronger but harder to implement     | Less secure (token-based), uses HTTPS |
| Access Type                | Direct resource access with user credentials | Can delegate access with user consent|
| Refresh Tokens             | No refresh tokens                    | Supports refresh tokens              |
| Supported Protocols        | HTTP-based only                      | Can support various protocols (JWT, etc.) |
| Response Type              | Request tokens via API calls         | Support for authorization grants (Auth Code, Implicit, etc.) |
| Use of Scopes              | Not fully implemented                 | Scopes help limit access permissions |

---

## 🔁 OAuth2 Flows

- **Client Credentials**: Machine-to-machine access (no user)
- **Authorization Code**: Used with user login and frontend apps
- **Implicit**: For browser-based apps (deprecated)
- **Password Grant**: Direct user credentials (not recommended)
- **Device Code**: For devices without browser input

---

# OAuth2 Flows for Pizza App

This document describes two OAuth2 flows used in the Pizza App: **Client Credentials Flow** and **Authorization Code Flow**.

---

## 🔐 1. Client Credentials Flow
![Diagram](Client_credentials_flow.png)

Used when the client (e.g., backend service) needs to access the API directly without user involvement.

### 🔸 Base64-Encoded Credentials
client_id:client_secret

```bash
echo -n "pizza-client:secret" | base64
# Output:
cGl6emEtY2xpZW50OnNlY3JldA==
```

To decode:
```bash
echo "cGl6emEtY2xpZW50OnNlY3JldA==" | base64 --decode
# Output:
pizza-client:secret
```

### 🔸 Token Request

**POST** `http://localhost:9000/oauth2/token`

**Headers:**
```
Authorization: Basic cGl6emEtY2xpZW50OnNlY3JldA==
Content-Type: application/x-www-form-urlencoded
```

**Body:**
```
grant_type=client_credentials
scope=api.read
```

### 🔸 Refresh Token Request

**POST** `http://localhost:9000/oauth2/token`

**Headers:**
```
Authorization: Basic cGl6emEtY2xpZW50OnNlY3JldA==
Content-Type: application/x-www-form-urlencoded
```

**Body:**
```
grant_type=grant_type
refresh_token={refresh_token_from_prev_token_response}
```

---

## 👤 2. Authorization Code Flow
![Diagram](Authorization_code_flow.png)

Used when a frontend client (e.g., a Vue app) wants access to user-protected resources via login and redirect.

### 🔸 Step 1: Get Authorization Code

Open in browser:

```
http://localhost:9000/oauth2/authorize?
  response_type=code&
  client_id=pizza-client&
  redirect_uri=http://localhost:5173/callback&
  scope=api.read
```

After user login and consent, you will receive a `code` in the redirect URL.

### 🔸 Step 2: Exchange Code for Access Token

**POST** `http://localhost:9000/oauth2/token`

**Headers:**
```
Authorization: Basic cGl6emEtY2xpZW50OnNlY3JldA==
Content-Type: application/x-www-form-urlencoded
```

**Body:**
```
grant_type=authorization_code
code={code_from_redirect_url}
redirect_uri=http://localhost:5173/callback
```

---

## 🔐 Bearer Token Usage

After obtaining the access token, use it to call secured endpoints by passing it in the `Authorization` header.

### 🍕 Get Pizzas (Resource Server)

**GET** `http://localhost:8080/api/pizzas`

**Headers:**
```
Authorization: Bearer {access_token}
```

### 👤 Get User Info (Authorization Server)

**GET** `http://localhost:9000/userinfo`

**Headers:**
```
Authorization: Bearer {access_token}
```