import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Card, Button } from '../components/UI';
import { getUserProfile, getSessions, getClients } from '../services/api';
import { ClipboardDocumentIcon, UserCircleIcon, CalendarIcon, UserGroupIcon } from '@heroicons/react/24/outline';

const DashboardPage: React.FC = () => {
  const [userProfile, setUserProfile] = useState<any>(null);
  const [sessionCount, setSessionCount] = useState<number>(0);
  const [clientCount, setClientCount] = useState<number>(0);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch user profile
        const profile = await getUserProfile();
        setUserProfile(profile);

        // Fetch sessions
        const sessions = await getSessions();
        setSessionCount(sessions.length);

        // Fetch clients
        const clients = await getClients();
        setClientCount(clients.length);
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  const setupSteps = [
    {
      title: "Complete Your Profile",
      description: "Add your personal and business details for invoices",
      isComplete: !!userProfile,
      link: "/profile",
      icon: UserCircleIcon,
    },
    {
      title: "Add Your Clients",
      description: "Create clients with contact details and hourly rates",
      isComplete: clientCount > 0,
      link: "/clients/new",
      icon: UserGroupIcon,
    },
    {
      title: "Log Your Sessions",
      description: "Record your billable sessions with date and time",
      isComplete: sessionCount > 0,
      link: "/sessions/new",
      icon: CalendarIcon,
    },
    {
      title: "Generate Your First Invoice",
      description: "Create a professional PDF invoice for a client",
      isComplete: userProfile && clientCount > 0 && sessionCount > 0,
      link: "/invoices",
      icon: ClipboardDocumentIcon,
    },
  ];

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <p className="text-gray-600">Loading dashboard...</p>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Welcome to VereinsKnete</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <Card className="flex flex-col items-center p-6 text-center">
          <UserGroupIcon className="w-12 h-12 text-blue-600 mb-2" />
          <span className="text-2xl font-bold">{clientCount}</span>
          <span className="text-gray-600">Clients</span>
        </Card>

        <Card className="flex flex-col items-center p-6 text-center">
          <CalendarIcon className="w-12 h-12 text-blue-600 mb-2" />
          <span className="text-2xl font-bold">{sessionCount}</span>
          <span className="text-gray-600">Sessions</span>
        </Card>

        <Card className="flex flex-col items-center p-6 text-center">
          <ClipboardDocumentIcon className="w-12 h-12 text-blue-600 mb-2" />
          <span className="text-2xl font-bold">-</span>
          <span className="text-gray-600">Invoices Generated</span>
        </Card>
      </div>

      <h2 className="text-xl font-semibold mb-4">Getting Started</h2>
      <Card>
        <ul>
          {setupSteps.map((step, index) => (
            <li
              key={index}
              className={`py-4 ${
                index < setupSteps.length - 1 ? 'border-b border-gray-200' : ''
              }`}
            >
              <div className="flex items-start">
                <div className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${
                  step.isComplete ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-400'
                }`}>
                  {step.isComplete ? (
                    <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  ) : (
                    <span>{index + 1}</span>
                  )}
                </div>
                <div className="ml-4 flex-1">
                  <div className="flex items-center justify-between">
                    <p className="font-medium">{step.title}</p>
                    <Link to={step.link}>
                      <Button variant={step.isComplete ? "secondary" : "primary"} className="text-sm py-1">
                        {step.isComplete ? 'Edit' : 'Start'}
                      </Button>
                    </Link>
                  </div>
                  <p className="mt-1 text-sm text-gray-600">{step.description}</p>
                </div>
              </div>
            </li>
          ))}
        </ul>
      </Card>
    </div>
  );
};

export default DashboardPage;
