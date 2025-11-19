# Capitalot

**Where Your Money Works Smarter**

A full-stack investment portfolio tracking application built with Spring Boot, Angular, and PostgreSQL.

## Features

- **Portfolio Management**: Create and manage multiple investment portfolios
- **Stock Tracking**: Track stocks with purchase price, quantity, and acquisition date
- **Watchlists**: Monitor stocks of interest with real-time search
- **Performance Analytics**: View daily, monthly, yearly, and all-time performance statistics
- **User Authentication**: Secure JWT-based authentication with OAuth2 Google integration ready
- **Tag System**: Organize and categorize stocks with custom tags

## Tech Stack

### Backend
- Spring Boot 3.2.0
- PostgreSQL 16
- Spring Security + JWT
- Spring Data JPA
- OAuth2 Client (Google ready)

### Frontend
- Angular 17
- TypeScript
- RxJS
- Angular Router with lazy loading

### Infrastructure
- Docker & Docker Compose
- Nginx (frontend reverse proxy)

## Quick Start

### Prerequisites
- Docker and Docker Compose
- (Optional) Java 17+ and Node.js 18+ for local development

### Running with Docker

1. Clone the repository:
```bash
git clone <repository-url>
cd capitalot
```

2. Copy environment file and configure:
```bash
cp .env.example .env
```

3. Start all services:
```bash
docker-compose up -d
```

4. Access the application:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5432

### Default Users

The application initializes with sample data:
- **Email**: demo@capitalot.com
- **Password**: password

## Project Structure

```
capitalot/
├── backend/                 # Spring Boot application
│   ├── src/main/java/com/capitalot/
│   │   ├── config/         # Security, data initialization
│   │   ├── controller/     # REST API endpoints
│   │   ├── dto/            # Data transfer objects
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data access layer
│   │   ├── security/       # JWT, user details
│   │   └── service/        # Business logic
│   └── src/main/resources/
│       └── application.yml # Configuration
├── frontend/                # Angular application
│   └── src/app/
│       ├── components/     # UI components
│       ├── guards/         # Route guards
│       ├── interceptors/   # HTTP interceptors
│       ├── models/         # TypeScript interfaces
│       └── services/       # API services
└── docker-compose.yml      # Container orchestration
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login with email/password
- `GET /api/auth/oauth2/authorization/google` - OAuth2 Google login

### Portfolios
- `GET /api/portfolios` - List user portfolios
- `POST /api/portfolios` - Create portfolio
- `PUT /api/portfolios/{id}` - Update portfolio
- `DELETE /api/portfolios/{id}` - Delete portfolio
- `POST /api/portfolios/{id}/stocks` - Add stock to portfolio
- `DELETE /api/portfolios/{portfolioId}/stocks/{stockId}` - Remove stock

### Watchlists
- `GET /api/watchlists` - List user watchlists
- `POST /api/watchlists` - Create watchlist
- `PUT /api/watchlists/{id}` - Update watchlist
- `DELETE /api/watchlists/{id}` - Delete watchlist
- `POST /api/watchlists/{id}/stocks` - Add stock to watchlist
- `DELETE /api/watchlists/{watchlistId}/stocks/{stockId}` - Remove stock

### Stocks
- `GET /api/stocks/search?query={query}` - Search stocks
- `GET /api/stocks/{symbol}/price` - Get current stock price

### Statistics
- `GET /api/stats/performance` - Get performance statistics

## Development

### Backend Development

```bash
cd backend
./mvnw spring-boot:run
```

Configuration in `backend/src/main/resources/application.yml`

### Frontend Development

```bash
cd frontend
npm install
npm start
```

Navigate to http://localhost:4200

### Database

Connect to PostgreSQL:
```bash
docker exec -it capitalot-postgres psql -U capitalot -d capitalot
```

## Configuration

### Environment Variables

See `.env.example` for all available configuration options:

- `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB` - Database credentials
- `JWT_SECRET` - Secret key for JWT token generation
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` - OAuth2 Google credentials

### Google OAuth2 Setup

1. Create OAuth2 credentials at [Google Cloud Console](https://console.cloud.google.com/)
2. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
3. Update `.env` with your client ID and secret
4. Restart the application

## Current Limitations

### Stub Services

The following services use stub implementations and should be replaced with real API integrations:

- **StockSearchService**: Returns hardcoded list of 10 popular stocks
- **StockPriceService**: Generates random prices between $50-$500

### Suggested API Integrations

- [Alpha Vantage](https://www.alphavantage.co/) - Stock data API
- [Finnhub](https://finnhub.io/) - Financial data API
- [Yahoo Finance API](https://www.yahoofinanceapi.com/) - Market data

## Security Notes

- JWT tokens are stored in browser localStorage
- All API requests require authentication (except login/register)
- CORS is configured for development (localhost:4200)
- For production, update CORS settings in `SecurityConfig.java`
- Change default JWT secret in production environment

## Docker Commands

```bash
docker-compose up -d          # Start all services
docker-compose down           # Stop all services
docker-compose logs -f        # View logs
docker-compose restart        # Restart services
docker-compose ps             # List running containers
```

## Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Roadmap

- [ ] Integrate real stock price API
- [ ] Add portfolio detail view with charts
- [ ] Implement stock price history tracking
- [ ] Add transaction history
- [ ] Create performance visualization charts
- [ ] Add portfolio diversification analysis
- [ ] Implement email notifications
- [ ] Add mobile responsive design
- [ ] Create export functionality (CSV, PDF)
- [ ] Add multi-currency support

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For questions or issues, please open an issue on GitHub.
