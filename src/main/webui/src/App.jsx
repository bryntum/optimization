import { useEffect, useState, useRef } from "react";
import { BryntumDemoHeader, BryntumGrid, BryntumSchedulerPro, BryntumSplitter } from "@bryntum/schedulerpro-react";

import { useSchedulerProConfig, useUnplannedGridConfig } from "./AppConfig";
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

    // Adds random tasks to the unplanned grid
    const onAddRandomTasks = (numberOfTasks) => {
        if (!unplannedGrid || !schedulerPro) return;

        const totalTaskCount = unplannedGrid.store.count + schedulerPro.project.eventStore.count;
        const possibleSkills = schedulerPro.project.getCrudStore('skills').records;
        const icons = ['bus', 'car', 'plane', 'ship', 'helicopter', 'rocket', 'truck', 'train'];

        const tasks = [];
        for (let i = 0; i < numberOfTasks; i++) {
            const numSkills = Math.floor(Math.random() * 3) + 1; // 1-3 skills
            const selectedSkills = possibleSkills
                .map(skill => skill.id)
                .sort(() => 0.5 - Math.random())
                .slice(0, numSkills); 

            const licensePlate = Math.random().toString(36).substring(2, 8).toUpperCase();
            const iconCls = `b-fa b-fa-${icons[Math.floor(Math.random() * icons.length)]}`;

            tasks.push({
                name: `Task ${totalTaskCount + i + 1}`,
                duration: Math.floor(Math.random() * 8) + 1, // 1-8 hours
                skills: selectedSkills,
                licensePlate: licensePlate,
                iconCls: iconCls
            });
        }

        unplannedGrid.store.add(tasks);
        schedulerPro.crudManager.sync();
    }

    const schedulerProConfig = useSchedulerProConfig(onSolve, onReset)
    const unplannedGridConfig = useUnplannedGridConfig(onAddRandomTasks)

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
    }, [])

    // Setup websocket as soon as schedulerPro is available
    useEffect(() => {
        const openWebsocket = async() => { 
            if (!schedulerPro) return;

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
                if(event.data.startsWith("Finished")) {
                    console.log("Done solving");
                }
                await schedulerPro.project.load();
            });
        }

        openWebsocket();
    }, [schedulerPro]);

    // Called the first time when project has loaded data and unplannedGrid also exists
    // Setup the store for unplannedGrid
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

    // Highlight resources which can perform selected task when task is selected in unplannedGrid 
    useEffect(() => {
        if (!unplannedGrid || !schedulerPro) return;

        unplannedGrid.on('selectionchange', ({ selected }) => {
            schedulerPro.highlightResourceCalendarsForEventRecords(selected);
        });
    }, [unplannedGrid, schedulerPro])

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