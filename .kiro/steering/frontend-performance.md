# Frontend Performance Guidelines

This document outlines performance optimization strategies, build optimization, and monitoring practices for the VereinsKnete frontend application.

## Build Optimization

### Current Build Status
- ✅ Bundle size: 137.56 kB (optimized)
- ✅ CSS size: 8.1 kB (Tailwind optimized)
- ✅ Chunk splitting: Enabled
- ✅ Tree shaking: Active

### Bundle Analysis
```bash
# Analyze bundle size
npm run build
npx webpack-bundle-analyzer build/static/js/*.js

# Monitor bundle size changes
npm install --save-dev bundlesize
```

### Code Splitting Strategies
```tsx
// Route-based code splitting
const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const ClientsPage = lazy(() => import('./pages/ClientsPage'));
const SessionsPage = lazy(() => import('./pages/SessionsPage'));

// Component-based code splitting for large components
const InvoiceGenerator = lazy(() => import('./components/InvoiceGenerator'));

// Wrap with Suspense
<Suspense fallback={<LoadingState />}>
  <Routes>
    <Route path="/" element={<DashboardPage />} />
    <Route path="/clients" element={<ClientsPage />} />
    <Route path="/sessions" element={<SessionsPage />} />
  </Routes>
</Suspense>
```

### Tree Shaking Optimization
```tsx
// Import only what you need
import { ChevronDownIcon } from '@heroicons/react/24/outline';

// Avoid importing entire libraries
// ❌ Bad
import * as _ from 'lodash';

// ✅ Good
import { debounce } from 'lodash';

// Use ES modules for better tree shaking
// ❌ Bad
const { format } = require('date-fns');

// ✅ Good
import { format } from 'date-fns';
```

## React Performance Optimization

### Memoization Patterns
```tsx
// Memoize expensive calculations
const ExpensiveComponent: React.FC<{ data: any[] }> = ({ data }) => {
  const processedData = useMemo(() => {
    return data.map(item => ({
      ...item,
      calculated: expensiveCalculation(item),
      formatted: formatCurrency(item.amount)
    }));
  }, [data]);

  return <div>{/* Render processed data */}</div>;
};

// Memoize callback functions to prevent unnecessary re-renders
const ParentComponent: React.FC = () => {
  const [count, setCount] = useState(0);
  const [filter, setFilter] = useState('');
  
  const handleIncrement = useCallback(() => {
    setCount(prev => prev + 1);
  }, []);

  const handleFilterChange = useCallback((newFilter: string) => {
    setFilter(newFilter);
  }, []);

  return (
    <div>
      <ChildComponent 
        onIncrement={handleIncrement}
        onFilterChange={handleFilterChange}
      />
    </div>
  );
};

// Memoize components to prevent unnecessary re-renders
const MemoizedClientCard = React.memo<ClientCardProps>(({ client, onEdit }) => {
  return (
    <Card>
      <h3>{client.name}</h3>
      <p>{client.address}</p>
      <Button onClick={() => onEdit(client.id)}>Edit</Button>
    </Card>
  );
});
```

### State Updates Optimization
```tsx
// Batch state updates (React 18 automatically batches)
const handleMultipleUpdates = () => {
  setLoading(true);
  setError(null);
  setData([]);
  // These are automatically batched in React 18
};

// Use functional updates for state based on previous state
const handleIncrement = () => {
  setCount(prevCount => prevCount + 1);
};

// Avoid creating objects in render
// ❌ Bad - creates new object on every render
const BadComponent = () => {
  return <ChildComponent style={{ margin: 10 }} />;
};

// ✅ Good - stable reference
const MARGIN_STYLE = { margin: 10 };
const GoodComponent = () => {
  return <ChildComponent style={MARGIN_STYLE} />;
};
```

### Virtual Scrolling for Large Lists
```tsx
// For very large datasets, consider virtual scrolling
import { FixedSizeList as List } from 'react-window';

const VirtualizedClientList: React.FC<{ clients: Client[] }> = ({ clients }) => {
  const Row = ({ index, style }: { index: number; style: React.CSSProperties }) => (
    <div style={style}>
      <ClientCard client={clients[index]} />
    </div>
  );

  return (
    <List
      height={600}
      itemCount={clients.length}
      itemSize={120}
      width="100%"
    >
      {Row}
    </List>
  );
};
```

## Data Fetching Optimization

### Caching Strategies
```tsx
// In-memory caching with custom hook
const useClientCache = () => {
  const [cache, setCache] = useState<Map<string, any>>(new Map());

  const getCachedData = useCallback((key: string) => {
    return cache.get(key);
  }, [cache]);

  const setCachedData = useCallback((key: string, data: any) => {
    setCache(prev => new Map(prev).set(key, {
      data,
      timestamp: Date.now(),
      ttl: 5 * 60 * 1000 // 5 minutes
    }));
  }, []);

  const isCacheValid = useCallback((key: string) => {
    const cached = cache.get(key);
    if (!cached) return false;
    return Date.now() - cached.timestamp < cached.ttl;
  }, [cache]);

  return { getCachedData, setCachedData, isCacheValid };
};

// Usage in data fetching
const useClients = () => {
  const { getCachedData, setCachedData, isCacheValid } = useClientCache();
  const [data, setData] = useState<Client[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchClients = useCallback(async () => {
    const cacheKey = 'clients';
    
    if (isCacheValid(cacheKey)) {
      const cached = getCachedData(cacheKey);
      setData(cached.data);
      return;
    }

    setLoading(true);
    try {
      const clients = await api.clients.getAll();
      setData(clients);
      setCachedData(cacheKey, clients);
    } finally {
      setLoading(false);
    }
  }, [getCachedData, setCachedData, isCacheValid]);

  return { data, loading, refetch: fetchClients };
};
```

### Request Deduplication
```tsx
// Prevent duplicate API calls
const requestCache = new Map<string, Promise<any>>();

const dedupedFetch = async <T>(key: string, fetcher: () => Promise<T>): Promise<T> => {
  if (requestCache.has(key)) {
    return requestCache.get(key);
  }

  const promise = fetcher().finally(() => {
    requestCache.delete(key);
  });

  requestCache.set(key, promise);
  return promise;
};

// Usage
const fetchClient = (id: number) => {
  return dedupedFetch(`client-${id}`, () => api.clients.getById(id));
};
```

### Pagination and Infinite Scroll
```tsx
// Efficient pagination
const usePaginatedClients = (pageSize = 20) => {
  const [data, setData] = useState<Client[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);

  const loadMore = useCallback(async () => {
    if (loading || !hasMore) return;

    setLoading(true);
    try {
      const newClients = await api.clients.getPage(currentPage, pageSize);
      
      if (newClients.length < pageSize) {
        setHasMore(false);
      }

      setData(prev => [...prev, ...newClients]);
      setCurrentPage(prev => prev + 1);
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, loading, hasMore]);

  return { data, loading, hasMore, loadMore };
};

// Infinite scroll implementation
const useInfiniteScroll = (callback: () => void) => {
  useEffect(() => {
    const handleScroll = () => {
      if (
        window.innerHeight + document.documentElement.scrollTop
        >= document.documentElement.offsetHeight - 1000
      ) {
        callback();
      }
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, [callback]);
};
```

## Image and Asset Optimization

### Image Optimization
```tsx
// Lazy loading images
const LazyImage: React.FC<{ src: string; alt: string }> = ({ src, alt }) => {
  const [isLoaded, setIsLoaded] = useState(false);
  const [isInView, setIsInView] = useState(false);
  const imgRef = useRef<HTMLImageElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsInView(true);
          observer.disconnect();
        }
      },
      { threshold: 0.1 }
    );

    if (imgRef.current) {
      observer.observe(imgRef.current);
    }

    return () => observer.disconnect();
  }, []);

  return (
    <div ref={imgRef} className="relative">
      {!isLoaded && (
        <div className="absolute inset-0 bg-gray-200 animate-pulse" />
      )}
      {isInView && (
        <img
          src={src}
          alt={alt}
          onLoad={() => setIsLoaded(true)}
          className={`transition-opacity duration-300 ${
            isLoaded ? 'opacity-100' : 'opacity-0'
          }`}
        />
      )}
    </div>
  );
};
```

### Font Optimization
```css
/* Preload critical fonts */
<link
  rel="preload"
  href="/fonts/inter-var.woff2"
  as="font"
  type="font/woff2"
  crossOrigin="anonymous"
/>

/* Font display optimization */
@font-face {
  font-family: 'Inter';
  src: url('/fonts/inter-var.woff2') format('woff2');
  font-display: swap; /* Improves loading performance */
}
```

## CSS and Styling Performance

### Tailwind CSS Optimization
```js
// tailwind.config.js - Purge unused styles
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
    "./public/index.html"
  ],
  theme: {
    extend: {
      // Only extend what you need
    }
  },
  plugins: [
    // Only include necessary plugins
  ]
}
```

### CSS-in-JS Performance
```tsx
// Avoid inline styles in render
// ❌ Bad - creates new object every render
const BadComponent = () => {
  return (
    <div style={{ padding: '1rem', margin: '0.5rem' }}>
      Content
    </div>
  );
};

// ✅ Good - use CSS classes
const GoodComponent = () => {
  return (
    <div className="p-4 m-2">
      Content
    </div>
  );
};
```

## Performance Monitoring

### Core Web Vitals Monitoring
```tsx
// Monitor performance metrics
const usePerformanceMonitoring = () => {
  useEffect(() => {
    // Largest Contentful Paint
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const lastEntry = entries[entries.length - 1];
      console.log('LCP:', lastEntry.startTime);
    }).observe({ entryTypes: ['largest-contentful-paint'] });

    // First Input Delay
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      entries.forEach((entry) => {
        console.log('FID:', entry.processingStart - entry.startTime);
      });
    }).observe({ entryTypes: ['first-input'] });

    // Cumulative Layout Shift
    new PerformanceObserver((list) => {
      let clsValue = 0;
      const entries = list.getEntries();
      entries.forEach((entry) => {
        if (!entry.hadRecentInput) {
          clsValue += entry.value;
        }
      });
      console.log('CLS:', clsValue);
    }).observe({ entryTypes: ['layout-shift'] });
  }, []);
};
```

### Performance Profiling
```tsx
// Profile component render times
const ProfiledComponent: React.FC = () => {
  useEffect(() => {
    const startTime = performance.now();
    
    return () => {
      const endTime = performance.now();
      console.log(`Component rendered in ${endTime - startTime}ms`);
    };
  });

  return <div>Component content</div>;
};

// Use React DevTools Profiler
const App = () => {
  return (
    <Profiler
      id="App"
      onRender={(id, phase, actualDuration) => {
        console.log('Render:', { id, phase, actualDuration });
      }}
    >
      <Router>
        <Routes>
          {/* Your routes */}
        </Routes>
      </Router>
    </Profiler>
  );
};
```

## Progressive Web App Features

### Service Worker Implementation
```tsx
// Register service worker
useEffect(() => {
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/sw.js')
        .then(registration => {
          console.log('SW registered:', registration);
        })
        .catch(error => {
          console.log('SW registration failed:', error);
        });
    });
  }
}, []);

// Offline detection
const useOnlineStatus = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  return isOnline;
};
```

### Caching Strategies
```js
// Service worker caching strategy
const CACHE_NAME = 'vereinsknete-v1';
const urlsToCache = [
  '/',
  '/static/js/bundle.js',
  '/static/css/main.css',
  '/manifest.json'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(urlsToCache))
  );
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request)
      .then((response) => {
        // Return cached version or fetch from network
        return response || fetch(event.request);
      })
  );
});
```

## Performance Best Practices

### General Guidelines
- Minimize bundle size through code splitting and tree shaking
- Use React.memo() for expensive components
- Implement proper caching strategies
- Optimize images and fonts
- Monitor Core Web Vitals
- Use lazy loading for non-critical resources
- Implement proper error boundaries to prevent cascading failures

### Development Tools
```bash
# Performance analysis tools
npm install --save-dev webpack-bundle-analyzer
npm install --save-dev lighthouse
npm install --save-dev @web/dev-server

# Bundle size monitoring
npm install --save-dev bundlesize

# Performance testing
npm install --save-dev @testing-library/react
npm install --save-dev @testing-library/jest-dom
```

This document should be referenced when working on performance optimization, build configuration, or monitoring in the VereinsKnete application.