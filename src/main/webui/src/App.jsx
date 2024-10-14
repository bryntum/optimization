import { useEffect, useState, useRef } from "react";
import { BryntumDemoHeader, BryntumGrid, BryntumSchedulerPro, BryntumSplitter } from "@bryntum/schedulerpro-react";

import { useSchedulerProConfig, unplannedGridConfig } from "./AppConfig";
import Task from "./lib/Task.js";
import Technician from "./lib/Technician.js";
import Skill from "./lib/Skill.js";
import Drag from "./lib/Drag.js";

import "./App.scss";

function App() {
    const schedulerProRef = useRef();
    const unplannedGridRef = useRef();
    const dragRef = useRef();

    const [schedulerPro, setSchedulerPro] = useState();
    const [unplannedGrid, setUnplannedGrid] = useState();

    useEffect(() => {
        setSchedulerPro(schedulerProRef.current?.instance);
        setUnplannedGrid(unplannedGridRef.current?.instance);
    }, [schedulerProRef, unplannedGridRef])

    const onSolve = async () => {
        if (!schedulerPro) return;

        const response = await fetch('api/solve', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        // Trigger load call instead of setting data inline so unplannedGrid's store also get's refreshed
        await schedulerPro.project.load();

        schedulerPro.zoomToFit();
    }

    const onReset = async () => { if (!schedulerPro) return;
        const response = await fetch('api/reset', {
            method: 'POST'
        })
        if (!response.ok) {
            throw new Error('Network Error. Unable to reset data')
        }
        
        await schedulerPro.project.load();
    }

    const openWebsocket = async() => { if (!schedulerPro) return;
        const { protocol, hostname, port } = window.location;
        const wsProtocol = protocol === 'https:' ? 'wss' : 'ws';
        const wsPort = port ? `:${port}` : '';
        const socket = new WebSocket(`${wsProtocol}://${hostname}${wsPort}/timefold`)

        // Connection opened
        socket.addEventListener("open", event => {
            console.log("Connected with websocket")
        });

        // Listen for messages
        socket.addEventListener("message", async event => {
            console.log("Update from server ", event.data)
            await schedulerPro.project.load();
        });
    }

    const schedulerProConfig = useSchedulerProConfig(onSolve, onReset)

    const [isProjectLoaded, setIsProjectLoaded] = useState(false);
    const [projectConfig] = useState({
        autoLoad: true,
        autoSync: true,
        loadUrl: 'api/read',
        syncUrl: 'api/sync',
        resourceStore: {
            modelClass: Technician,
            sorters: [{field: 'name', ascending: true}]
        },
        eventStore: {
            modelClass: Task
        },
        crudStores: [
            {
                id: 'skills',
                modelClass: Skill
            },
            {
                id: 'unplanned',
                modelClass: Task,
                reapplySortersOnAdd: true
            }
        ],
        onLoad: () => {
            setIsProjectLoaded(true);
        }
    }) 

    // Only called on initial page load to reset data
    useEffect(() => {
        onReset();
        openWebsocket()
    }, [])

    // Called the first time when project has loaded data and unplannedGrid also exists
    useEffect(() => {
        if (!isProjectLoaded || !unplannedGrid) return;

        unplannedGrid.store = schedulerPro.project.getCrudStore('unplanned');
    }, [isProjectLoaded, unplannedGrid])

    // Attach a DragHelper to SchedulerPro and UnplannedGrid Instance
    useEffect(() => {
        if (!schedulerPro || !unplannedGrid) {
            return;
        }

        dragRef.current = new Drag({
            grid         : unplannedGrid,
            schedule     : schedulerPro,
            constrain    : false,
            outerElement : unplannedGrid.element
        });

        // We need to destroy Drag instance because React 18 Strict mode
        // runs this component twice in development mode and Drag has no
        // UI so it is not destroyed automatically as grid and scheduler.
        return () => dragRef.current?.destroy?.();
    }, [unplannedGrid, schedulerPro, dragRef]);

    return (
        <>
            <BryntumDemoHeader/>
            <div id="content">
                <BryntumSchedulerPro 
                    ref={schedulerProRef}
                    {...schedulerProConfig}
                    project={projectConfig}
                />
                <BryntumSplitter/>
                <BryntumGrid
                    ref={unplannedGridRef}
                    {...unplannedGridConfig}
                />
            </div>
        </>
    )
}

export default App;