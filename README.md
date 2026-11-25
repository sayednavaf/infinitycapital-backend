# Infinity Capital API

Spring Boot 3 REST API for the Infinity Capital React/Vite frontend. Advisory service descriptions are stored in PostgreSQL. Market and stock-analysis data are obtained only from a configured, authorized upstream provider; the API does not generate quotes or sample prices.

## Requirements

- Java 17+
- Maven 3.9+
- PostgreSQL 14+

Create a PostgreSQL database named `infinitycapital`, then configure environment variables from `.env.example` in your shell or IDE run configuration. In particular, set `DATABASE_PASSWORD` to your local PostgreSQL password. Do not commit credentials. The frontend is served from `http://localhost:5173` by default; update `FRONTEND_ORIGIN` when it runs elsewhere.

Run the API from this directory:

```powershell
mvn spring-boot:run
```

Flyway creates the PostgreSQL schema and inserts the seven advisory catalog entries with the initial migration. Hibernate validates the schema at startup.

## Market provider integration

No market-data provider credentials or existing API were present in the inspected project. Configure `MARKET_DATA_API_URL` as a trusted server-side provider base URL. The backend requests:

- `GET {MARKET_DATA_API_URL}/snapshot?period=1D|1W|1M|3M|1Y`
- `GET {MARKET_DATA_API_URL}/stocks/{symbol}/analysis`

If needed, set `MARKET_DATA_API_TOKEN` and `MARKET_DATA_SOURCE`; the token is sent only as an upstream Bearer token and is never returned to the frontend. The upstream should return normalized JSON using the frontend contract. A snapshot contains `quotes` and `series` arrays, with optional `instruments`, `gainers`, `losers`, `marketStatus`, `volume`, `source`, and ISO-8601 `updatedAt` fields. Quotes use `symbol`, `name`, `type`, `exchange`, `price`, `change`, `changePercent`, `volume`, and `timestamp`; chart points use `time` and `value`. Stock analysis returns `symbol`, `summary`, `source`, and `updatedAt`.

When the provider is not configured, unavailable, or returns an unsupported response, market endpoints return HTTP 503 with a message. The frontend renders an unavailable state rather than invented live data. Connect a licensed provider and map its vendor-specific response to this normalized contract before enabling live quotes.

## API endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/market/snapshot?period=1D` | Provider-backed index/stock quotes, chart data, market status and movers |
| GET | `/api/dashboard` | Dashboard market snapshot |
| GET | `/api/advisory/services` | Active service catalog from PostgreSQL |
| GET | `/api/stocks/{symbol}/analysis` | Provider-backed stock analysis |

The market snapshot supports `1D`, `1W`, `1M`, `3M`, and `1Y`. Market volume, top gainers/losers, and instrument prices are shown only when supplied by the upstream source.
