import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import { LanguageProvider } from './i18n';
import { ToastProvider } from './components/Toast';
import { ConfirmProvider } from './components/ConfirmDialog';

// Pages
import DashboardPage from './pages/DashboardPage';
import ProfilePage from './pages/ProfilePage';
import ClientsPage from './pages/ClientsPage';
import ClientFormPage from './pages/ClientFormPage';
import SessionsPage from './pages/SessionsPage';
import SessionFormPage from './pages/SessionFormPage';
import InvoicesPage from './pages/InvoicesPage';

function App() {
  return (
    <LanguageProvider>
      <ToastProvider>
        <ConfirmProvider>
          <Router>
            <Layout>
              <Routes>
                <Route path="/" element={<DashboardPage />} />
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/clients" element={<ClientsPage />} />
                <Route path="/clients/:id" element={<ClientFormPage />} />
                <Route path="/clients/new" element={<ClientFormPage />} />
                <Route path="/sessions" element={<SessionsPage />} />
                <Route path="/sessions/:id" element={<SessionFormPage />} />
                <Route path="/sessions/new" element={<SessionFormPage />} />
                <Route path="/invoices" element={<InvoicesPage />} />
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </Layout>
          </Router>
        </ConfirmProvider>
      </ToastProvider>
    </LanguageProvider>
  );
}

export default App;
