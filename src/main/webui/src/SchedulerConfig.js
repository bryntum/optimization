/**
 * Application configuration
 */
import Task from "./lib/Task.js";
import Technician from "./lib/Technician.js";
import Skill from "./lib/Skill.js";

const schedulerProps = {
    multiEventSelect: true,

    startDate : new Date(2024, 10, 4),
    endDate   : new Date(2024, 10, 9),
    flex      : 1,

    timeResolution: {
        magnitude: 1,
        unit: 'd'
    },
    eventStyle: 'rounded',
    rowHeight: 65,
    barMargin: 7,
    tickSize: 150,
    fillTicks: true,
    eventColor: 'indigo',
    useInitialAnimation: false,
    zoomOnMouseWheel: false,
    zoomOnTimeAxisDoubleClick: false,
    title: 'Planned maintenance activities',
    ui: 'toolbar',

    viewPreset: {
        base: 'dayAndWeek',
        shiftUnit: 'week',
        headers: [
            {
                unit: 'd',
                align: 'center',
                dateFormat: 'ddd DD'
            }
        ]
    },


    columns: [{text: 'Name', field: 'name', width: 130}],

    project: {
        crudManager: {
            autoLoad: true,
            loadUrl: './api/read',
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
            ]
        }
    },
    tbar: [
        {
            text: "Solve",
            async onClick() {
                const response = await fetch('./api/solve', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                const data = await response.json();
                const scheduler = this.up("schedulerPro")
                scheduler.eventStore.data = data.events.rows;
                scheduler.resourceStore.data = data.resources.rows;
                scheduler.zoomToFit();
            }
        }
    ]
};

export {schedulerProps};