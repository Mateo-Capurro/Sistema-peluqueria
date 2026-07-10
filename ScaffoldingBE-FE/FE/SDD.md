# SDD - Frontend (Scaffolding de Autenticación)

## 1. Objetivo del Frontend
SPA Angular que consume API REST backend. 
Maneja autenticación JWT (access + refresh token), sesión persistente, y sirve como scaffolding para futuros proyectos. 
UX responsive con Tailwind v3. Sin testing.

---

## 2. Stack Tecnológico (Inamovible)

| Capa | Tecnología | Versión |
| :--- | :--- | :--- |
| Framework | Angular (standalone) | ^22.0.0 |
| Lenguaje | TypeScript | ~6.0.2 |
| Estilos | Tailwind CSS | v3 |
| HTTP | RxJS (Observable) | ~7.8.0 |
| Router | @angular/router (lazy loading) | ^22.0.0 |
| Forms | @angular/forms (ReactiveFormsModule) | ^22.0.0 |
| Build | @angular/build (esbuild/Vite) | ^22.0.4 |
| Node | Node.js | 18+ |

---

## 3. Estructura de Directorios (Obligatoria)

```
├── public/                     # Recursos estáticos públicos (assets, imágenes, íconos, fuentes, etc.)
│   └── assets/
│
└── src/
    ├── index.html
    ├── main.ts                          # bootstrapApplication(App, appConfig)
    ├── styles.css                       # Tailwind directives
    └── app/
        ├── app.routes.ts                  # Configuración de rutas (lazy loading).
        ├── app.config.ts                  # Configuración de providers (router, http interceptors
        ├── app.css                        # Estilos globales (Tailwind).
        ├── app.html                       # Root component template (router-outlet).
        ├── app.ts                       # Root component (standalone).
        │
        ├── core/               # Elementos globales y singleton utilizados en toda la aplicación.
        │   ├── services/       # Servicios compartidos (API, autenticación, configuración, etc.).
        │   ├── guards/         # Guards para proteger rutas.
        │   ├── interceptors/   # Interceptores HTTP.
        │   ├── models/         # Modelos y tipos de dominio globales.
        │   ├── layouts/        # Layouts principales (Shell, Auth, Dashboard, etc.).
        │   └── utils/          # Utilidades y helpers reutilizables.
        │
        ├── components/           # Componentes específicos de cada feature o módulo.
        │   ├── components-a/      
        │   ├── components-b/
        │   └── ...
        │
        └── shared/             # Recursos reutilizables por múltiples features.
            ├── components/     # Componentes UI reutilizables.
            ├── directives/     # Directivas compartidas.
            ├── pipes/          # Pipes reutilizables.
            ├── models/         # Interfaces y modelos compartidos.
            ├── utils/          # Funciones auxiliares reutilizables.
            └── constants/      # Constantes, enums y configuraciones comunes.

```

---

## 4. Routing (app.routes.ts)

```typescript
export const routes: Routes = [
  {
    path: 'auth',
    loadComponent: () => import('./components/auth-layout/auth-layout'),
    children: [
      { path: 'login',    loadComponent: () => import('./components/login/login') },
      { path: 'register', loadComponent: () => import('./components/register/register') },
      { path: '**',       redirectTo: 'login' }
    ]
  },
  {
    path: '',
    loadComponent: () => import('./components/main-layout/main-layout'),
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: 'auth/login' }
];
```

### Guards

| Guard | Lógica |
| :--- | :--- |
| `authGuard` | `inject(AuthService).isAuthenticated()` ? true : `redirectTo('/auth/login')` |

---

## 5. Flujo de Autenticación

```
[Login/Register]
      │ POST /api/auth/authenticate
      ▼
[Backend] → AuthResponse { accessToken, refreshToken, username, role }
      │
      ▼
[AuthService]
  ├─ Almacena accessToken en localStorage
  ├─ Almacena refreshToken en localStorage
  ├─ Almacena username + role en estado señal (signal)
  └─ Redirige a /

[Request protegido]
      │
      ▼
[AuthInterceptor]
  ├─ Lee accessToken de localStorage
  ├─ Añade header: Authorization: Bearer {token}
  └─ Forward request

[Response 401]
      │
      ▼
[ErrorInterceptor]
  ├─ Si hay refreshToken → POST /api/auth/refresh
  │   ├─ OK → almacena nuevos tokens → retry request original
  │   └─ FAIL → logout (limpia storage → redirect /auth/login)
  └─ Si no hay refreshToken → logout directo
```

### 5.1. AuthService (métodos clave)

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  // Signals reactivos
  username = signal<string | null>(null);
  role = signal<'USER' | 'ADMIN' | null>(null);

  login(req: LoginRequest): Observable<AuthResponse>
  register(req: RegisterRequest): Observable<AuthResponse>
  refresh(): Observable<AuthResponse>
  logout(): void
  isAuthenticated(): boolean
  getToken(): string | null
  getRefreshToken(): string | null
}
```

---

## 6. Modelos TypeScript

### 6.1. auth.model.ts

```typescript
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  name: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  username: string;
  role: 'USER' | 'ADMIN';
}
```

### 6.2. user.model.ts

```typescript
export interface UserResponse {
  id: number;
  username: string;
  name: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
}
```

---

## 7. Componentes — Especificación

Cada componente tiene 3 archivos: `nombre.ts`, `nombre.html`, `nombre.css`.

### 7.1. AuthLayout

| Archivo | Propósito |
| :--- | :--- |
| `.ts` | Router outlet para páginas de auth |
| `.html` | Contenedor centrado (flex), título arriba, `<router-outlet>` |
| `.css` | Responsive: padding/margins según breakpoint |

### 7.2. MainLayout

| Archivo | Propósito |
| :--- | :--- |
| `.ts` | Header con username, botón logout |
| `.html` | Header fijo top, `<router-outlet>` main content |
| `.css` | Contenedor responsive |

### 7.3. Login

| Archivo | Propósito |
| :--- | :--- |
| `.ts` | ReactiveForm (username, password). Submit → `AuthService.login()` |
| `.html` | Card centrada, input group, error message, link a register |
| `.css` | Responsive: full-width en mobile, max-w-md en desktop |

### 7.4. Register

| Archivo | Propósito |
| :--- | :--- |
| `.ts` | ReactiveForm (username, name, password). Validación: username 3-50, password min 6 |
| `.html` | Card centrada, input group, link a login |
| `.css` | Responsive: igual que login |

---

## 8. Environment Config

### `src/environments/environment.ts`

```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080',
  tokenKey: 'access_token',
  refreshTokenKey: 'refresh_token'
};
```

Proxy config para dev (`proxy.conf.json`):

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

---

## 9. App Config (app.config.ts)

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authInterceptor, errorInterceptor])
    )
  ]
};
```

---

## 10. Comandos de Ejecución

```bash
# Levantar en desarrollo (Angular dev server + proxy a backend)
ng serve

# Build producción
ng build --configuration production
```

---

## 11. Restricciones Técnicas NO NEGOCIABLES

- ✅ **Sin NgModules.** Solo standalone components.
- ✅ **Sin testing.**
- ✅ **CSS solo para responsive.** Tailwind v3 clases en HTML son fuente de estilo principal.
- ✅ **3 archivos por componente:** `nombre.ts`, `nombre.html`, `nombre.css`.
- ✅ **Routing lazy loading.**
- ✅ **Interceptor funcional.** Usar `HttpInterceptorFn`.
- ✅ **Refresh token rotation.**
- ✅ **Tokens no expuestos.**
- ✅ **Proxy en desarrollo.** `proxy.conf.json` apunta a `localhost:8080`.
- ✅ **Forms reactivos.** No usar template-driven forms.
- ✅ **Señales para estado de auth.** Signals (`username`, `role`) para reactividad.
