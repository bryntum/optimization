import { useEffect, useState, useRef } from "react";
import { BryntumDemoHeader, BryntumGrid, BryntumSchedulerPro, BryntumSplitter } from "@bryntum/schedulerpro-react";
import { Mask, Popup } from "@bryntum/schedulerpro";

import { useSchedulerProConfig, useUnplannedGridConfig } from "./AppConfig";
import Task from "./lib/Task.js";
import Technician from "./lib/Technician.js";
import Skill from "./lib/Skill.js";
import Drag from "./lib/Drag.js";
import { generateRandomTasks } from "./utils";

import "./App.scss";

import timefoldNegativeLogo from '../public/timefold-logomark-negative.svg';

function App() {
    const schedulerProRef = useRef();
    const unplannedGridRef = useRef();
    const dragRef = useRef();
    const addTechnicianPopup = useRef();

    const [schedulerPro, setSchedulerPro] = useState();
    const [solveStatus, setSolveStatus] = useState('pending'); // Can be 'pending', 'solving', or 'finished'
    const [unplannedGrid, setUnplannedGrid] = useState();

    useEffect(() => {
        setSchedulerPro(schedulerProRef.current?.instance);
        setUnplannedGrid(unplannedGridRef.current?.instance);
    }, [schedulerProRef, unplannedGridRef])

    const onSolve = async () => {
        if (!schedulerPro) return;

        setSolveStatus('solving');
        const response = await fetch('api/solve', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
    }

    useEffect(() => {
        if (!schedulerPro) return;

        let mask;
        if (solveStatus === 'solving') {
            schedulerPro.tools.solveButton.icon = 'b-fa b-fa-spinner'
            mask = Mask.mask({
                text: "Solving...",
                appendTo: 'content'
            })
        }
        else if (solveStatus === 'finished') {
            schedulerPro.tools.solveButton.icon = 'b-fa b-fa-check'
            Mask.unmask();
        } 
        else { // isSolving === 'pending'
            schedulerPro.tools.solveButton.icon = 'b-fa b-fa-wand-magic-sparkles'
        }

        () => {
            mask.destroy();
        }
    }, [schedulerPro, solveStatus])

    const onReset = async () => { 
        if (!schedulerPro) return;
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

        const tasks = generateRandomTasks({numberOfTasks, totalTaskCount, possibleSkills, icons});

        unplannedGrid.store.add(tasks);
        schedulerPro.crudManager.sync();
        setSolveStatus('pending');
    };


    const schedulerProConfig = useSchedulerProConfig(onSolve, onReset, () => {
        addTechnicianPopup.current?.show()
    });
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
        onLoad: (args) => {
            setIsProjectLoaded(true);
        }
    }) 

    // Reset the data in backend when page is reloaded 
    useEffect(() => {
        onReset();
    }, [])

    // Setup websocket as soon as schedulerPro is available
    // We use websocket to get updates from the server when solving
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
                    setSolveStatus('finished');
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

    // Skills combo in the event editor needs the list of skills from backend which we set here
    useEffect(() => {
        if (!schedulerPro || !isProjectLoaded) return;
        schedulerPro.features.taskEdit.items.generalTab.items.skillField.store = schedulerPro.project.getCrudStore('skills');
    }, [schedulerPro, isProjectLoaded])

    // Update score whenever project loads new data
    useEffect(() => {
        if (!schedulerPro) return;

        const setScore = (score) => {
            schedulerPro.tools.scoreLabel.text = `Score: ${score}`;
        }

        schedulerPro.project.on('load', ({ response }) => {
            setScore(response.scoreAnalysis.score);
        });

        schedulerPro.project.on('sync', ({ response }) => {
            setScore(response.scoreAnalysis.score);
        });
    }, [schedulerPro])

    // Add Technician Popup
    useEffect(() => {
        if (!schedulerPro || !isProjectLoaded) return;

        const popup = new Popup({
            title: 'New Technician',
            autoShow: false,
            hidden: true,
            centered: true,
            width: '30em',
            modal: {
                closeOnMaskTap: true,
            },
            items: [
                {
                    type: 'text',
                    label: 'Name',
                    name: 'name',
                    required: true
                },
                {
                    type: 'combo',
                    ref : 'skillField',
                    idField      : 'id',
                    displayField : 'name',
                    label        : 'Skills',
                    name         : 'skills',
                    multiSelect: true,
                }
            ],
            bbar: {
                items: {
                    submit: {
                        text: 'Add',
                        onAction: () => {
                            const values = popup.getValues();
                            schedulerPro.project.getCrudStore('resources').add({
                                name: values.name,
                                skills: values.skills,
                                type: "Technicians",
                                calendar: "dayshift",
                            });
                            popup.hide();
                        } 
                    }
                }
            }

        }) 
        // Add Technician Popup needs the list of skills from backend which we set here
        popup.widgetMap.skillField.items = schedulerPro.project.getCrudStore('skills');

        addTechnicianPopup.current = popup;
        
        return () => {
            popup.destroy();
        }
    }, [schedulerPro, isProjectLoaded])

    return (
        <>
            <BryntumDemoHeader title={
                <>Bryntum + <img src={timefoldNegativeLogo} alt="Timefold Logo"/> Timefold for Skill Matching</>
            }/>
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