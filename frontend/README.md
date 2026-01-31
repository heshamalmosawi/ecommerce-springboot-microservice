# eSouq Frontend

Angular 20 e-commerce application for the eSouq microservices platform.

## Overview

This is a modern e-commerce frontend built with Angular 20, TypeScript, and SCSS. It provides a complete shopping experience for buyers and seller tools for managing products and orders.

## Key Features

### Buyer Features
- **Product Browsing**: Search, filter, and browse products by category
- **Shopping Cart**: Add items, manage quantities, view totals
- **Checkout**: Secure checkout with shipping information
- **Order History**: View past orders with status tracking
- **Purchase Analytics**: Insights into spending habits and product preferences
- **Order Management**: View order details and status history
- **Reorder**: Quick repurchase from delivered orders
- **Order Cancellation**: Cancel pending/processing orders

### Seller Features
- **Product Management**: Add, edit, and manage product listings
- **Product Analytics**: Track views, sales, and performance
- **Order Management**: View and manage orders containing seller's products
- **Status Updates**: Update order status (PENDING → PROCESSING → SHIPPED → DELIVERED)
- **Order Cancellation**: Cancel orders before shipping
- **Seller Analytics**: Revenue tracking, best-selling products, sales insights
- **Sales Dashboard**: Overview of sales performance and metrics

### Authentication & Security
- **User Registration**: Sign up with email and phone number
- **User Login**: JWT-based authentication
- **Role-Based Access**: Buyer and Seller roles with appropriate features
- **Profile Management**: View and update user profile
- **Session Management**: Automatic token refresh and logout

### UI/UX Features
- **Responsive Design**: Mobile-first approach with desktop optimization
- **Material Design**: Angular Material components for consistent UI
- **Dark Mode**: Theme support for comfortable viewing
- **Loading States**: Smooth loading indicators for all async operations
- **Error Handling**: User-friendly error messages and recovery options
- **Form Validation**: Client-side validation with clear error messages

## Architecture

### Technology Stack
- **Framework**: Angular 20.1.4
- **Language**: TypeScript
- **Styling**: SCSS with Angular Material
- **State Management**: Services with RxJS Observables
- **HTTP**: Angular HttpClient with interceptors
- **Routing**: Angular Router with route guards
- **Forms**: Reactive Forms with validation

### Application Structure

```
src/
├── app/
│   ├── components/          # Feature components
│   │   ├── auth/         # Authentication (login/register)
│   │   ├── home/         # Product listing page
│   │   ├── product-detail/# Product details
│   │   ├── cart/         # Shopping cart
│   │   ├── checkout/      # Checkout flow
│   │   ├── order-confirmation/ # Order success page
│   │   ├── order-history/ # Buyer order history
│   │   ├── order-actions/ # Order action buttons
│   │   ├── order-status-modal/ # Status update modal
│   │   ├── order-cancel-modal/ # Cancellation modal
│   │   ├── order-status-history/ # Status timeline
│   │   ├── reorder-modal/ # Reorder from past orders
│   │   ├── purchase-analytics/ # Buyer analytics dashboard
│   │   ├── seller-analytics/ # Seller analytics dashboard
│   │   ├── seller-orders/ # Seller order management
│   │   ├── add-product/  # Create new product
│   │   ├── edit-product/ # Edit existing product
│   │   ├── profile/      # User profile
│   │   └── layout/       # Main layout with navigation
│   ├── services/          # Data services
│   │   ├── auth.service.ts
│   │   ├── product.service.ts
│   │   ├── order.service.ts
│   │   ├── cart.service.ts
│   │   └── user.service.ts
│   ├── guards/            # Route protection
│   │   ├── auth-guard.ts
│   │   └── role-guard.ts
│   ├── interceptors/      # HTTP interceptors
│   │   └── auth.interceptor.ts
│   ├── models/            # TypeScript interfaces
│   └── app.routes.ts      # Routing configuration
├── environments/          # Environment configs
├── assets/              # Static assets
└── styles/             # Global styles
```

## Routes & Navigation

### Public Routes
| Route | Component | Description |
|-------|-----------|-------------|
| `/` | Home | Product listing with search and filters |
| `/auth` | Auth | Login and registration |
| `/products/:id` | ProductDetail | Product details page |
| `/cart` | Cart | Shopping cart management |

### Authenticated Routes (All Users)
| Route | Component | Description |
|-------|-----------|-------------|
| `/profile` | Profile | User profile management |
| `/profile/order-history` | OrderHistory | View past orders |
| `/profile/analytics` | PurchaseAnalytics | Purchase insights |
| `/checkout` | Checkout | Checkout flow |
| `/order-confirmation` | OrderConfirmation | Order success page |

### Seller Routes
| Route | Component | Description | Required Role |
|-------|-----------|-------------|---------------|
| `/seller/analytics` | SellerAnalytics | Sales dashboard | SELLER |
| `/seller/orders` | SellerOrders | Manage orders | SELLER |
| `/products/add` | AddProduct | Create product | SELLER |
| `/products/:id/edit` | EditProduct | Edit product | SELLER |

## Services

### AuthService
Handles user authentication and session management.
- `login(email, password)` - User login
- `register(userData)` - User registration
- `logout()` - User logout
- `isAuthenticated()` - Check auth status
- `getUserRole()` - Get current user role

### ProductService
Manages product data and operations.
- `getProducts(filters)` - Get filtered products
- `getProductById(id)` - Get product details
- `createProduct(product, token)` - Add new product (seller)
- `updateProduct(id, product, token)` - Update product (seller)
- `deleteProduct(id, token)` - Delete product (seller)

### OrderService
Handles order-related operations.
- `createOrder(orderData, token)` - Create new order
- `getUserOrders(filters)` - Get user orders
- `getOrderById(orderId)` - Get order details
- `updateOrderStatus(orderId, status)` - Update status (seller)
- `cancelOrder(orderId, reason)` - Cancel order
- `getItemsForReorder(orderId)` - Get items for reorder
- `getSellerOrders(filters)` - Get seller orders
- `getPurchaseAnalytics(filters)` - Buyer analytics
- `getSellerAnalytics(filters)` - Seller analytics

### CartService
Manages shopping cart state and operations.
- `addToCart(product)` - Add product to cart
- `removeFromCart(productId)` - Remove from cart
- `updateQuantity(productId, quantity)` - Update item quantity
- `clearCart()` - Empty cart
- `getCart()` - Get current cart
- `getTotal()` - Calculate cart total

### UserService
Manages user data and profile.
- `getProfile(token)` - Get user profile
- `updateProfile(data, token)` - Update profile
- `getUsers()` - Get all users (admin)

## Authentication Flow

1. User navigates to `/auth`
2. Enters credentials (email, password, phone, name)
3. Service sends POST request to `/api/auth/register` or `/api/auth/login`
4. Backend validates and returns JWT token
5. Frontend stores token in localStorage
6. AuthInterceptor adds token to all subsequent requests
7. AuthGuard protects routes requiring authentication
8. RoleGuard restricts routes based on user role (BUYER/SELLER)

## Development

### Prerequisites
- Node.js 18+
- npm 9+
- Angular CLI 20.1.4

### Installation
```bash
cd frontend
npm install
```

### Running Development Server
```bash
ng serve
```

Application will be available at `http://localhost:4200`

### Building for Production
```bash
ng build --configuration production
```

Build artifacts will be in `dist/` directory.

### Running Tests
```bash
# Unit tests
npm test

# E2E tests
ng e2e

# Code coverage
ng test --coverage
```

### Linting
```bash
ng lint
```

## Environment Configuration

### Development (src/environments/environment.ts)
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### Production (src/environments/environment.prod.ts)
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.esouq.com/api'
};
```

## Styling

### Global Styles
- SCSS with CSS variables for theming
- Responsive breakpoints:
  - Mobile: < 768px
  - Tablet: 768px - 1024px
  - Desktop: > 1024px

### Angular Material
- Pre-built Material components for consistency
- Custom themes in `styles.scss`
- Icons from `@angular/material/icon`

### Component Styles
- Scoped component styles (`.scss` files)
- BEM naming convention for class names
- Flexbox and CSS Grid for layouts

## Error Handling

### HTTP Interceptor
- Catches all HTTP errors
- Shows user-friendly messages
- Redirects to login on 401 errors
- Logs errors to console for debugging

### User Feedback
- Snackbars for success/error messages
- Loading spinners for async operations
- Error messages on form fields
- Empty state messages for no data

## Performance

### Optimization
- Lazy loading for routes
- Image optimization and lazy loading
- OnPush change detection strategy
- TrackBy in ngFor loops
- Bundle splitting with Angular CLI

### Best Practices
- Component reusability
- Service layer for data management
- RxJS operators for data streams
- Memoization for expensive computations
- Virtual scrolling for long lists

## Accessibility

- ARIA labels on interactive elements
- Keyboard navigation support
- Screen reader compatibility
- Color contrast ratios (WCAG AA)
- Focus management in modals
- Alt text for images

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
- Mobile browsers (iOS Safari, Chrome Mobile)

## Testing

### Unit Testing
- Jasmine test framework
- TestBed for component testing
- Mock services with Jest or spies
- Component rendering and interaction tests

### E2E Testing
- Protractor or Cypress
- User flow testing
- Cross-browser testing
- CI/CD integration

### Code Coverage
- Istanbul for coverage reporting
- Target: 80% coverage
- Generate reports with `ng test --coverage`

## Deployment

### Docker Deployment
```bash
docker build -t esouq-frontend .
docker run -p 2122:2122 esouq-frontend
```

### Environment Variables
- `NODE_ENV`: Production/Development
- `API_URL`: Backend API URL
- `PORT`: Server port (default: 2122)

## API Integration

All frontend services communicate with backend via API Gateway:

**Base URL**: Configured in environment files
**Authentication**: JWT Bearer token in Authorization header
**Error Handling**: Standard HTTP status codes

### Key API Endpoints Used
- Authentication: `/api/auth/*`
- Products: `/api/products/*`
- Orders: `/api/orders/*`
- Users: `/api/users/*`
- Media: `/api/media/*`

## References

- [Backend API Documentation](../backend/)
- [Order API Spec](./REORDER_API_SPEC.md)
- [Main Project README](../README.md)
- [Angular Documentation](https://angular.dev)
- [Angular Material](https://material.angular.io)

## Contributing

1. Follow Angular style guide
2. Write tests for new features
3. Update documentation
4. Run linting and tests before committing
5. Follow git commit conventions

## Support

For questions or issues:
- Check [Main Project README](../README.md)
- Review [Code Review Guidelines](../CODE_REVIEW_GUIDELINES.md)
- Contact development team
