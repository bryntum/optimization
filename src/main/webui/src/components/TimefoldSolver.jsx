import React, { useState } from 'react';

const TimefoldSolver = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [data, setData] = useState(null);

    const fetchData = async () => {
        setIsLoading(true);
        try {
            const response = await fetch('./api/solve', { // Replace with your backend endpoint
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }});
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            setData(data);
        } catch (error) {
            console.error('Error fetching data:', error);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div>
            <button onClick={fetchData} disabled={isLoading}>
                {isLoading ? 'Loading...' : 'Solve with Timefold'}
            </button>
            {data && (
                <div>
                    <h3>Data:</h3>
                    <pre>{JSON.stringify(data, null, 2)}</pre>
                </div>
            )}
        </div>
    );
};

export default TimefoldSolver;