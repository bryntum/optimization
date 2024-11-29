import { StringHelper, DateHelper } from "@bryntum/schedulerpro";

/**
 * Application configuration
 */
const useSchedulerProConfig = (onSolve, onReset) => {
    return {
        startDate : new Date(2024, 10, 4),
        endDate   : new Date(2024, 10, 9),
        flex      : 1,
        multiEventSelect: true,
        eventStyle: 'rounded',
        rowHeight: 65,
        barMargin: 7,
        tickSize: 150,
        fillTicks: true,
        eventColor: 'indigo',
        useInitialAnimation: false,
        dependenciesFeature: false,
        createEventOnDblClick: {
            useEventModelDefaults: true
        },
        scheduleTooltipFeature: false,
        title: 'Planned maintenance activities',
        ui: 'toolbar',

        eventMenuFeature : {
            items : {
                splitEvent : false,
                unassign   : {
                    icon : 'b-fa b-fa-calendar-xmark',
                    text : 'Move to unplanned list',
                    onItem({ eventRecord, source }) {
                        const { project } = eventRecord;
                        eventRecord.remove();

                        project.getCrudStore('unplanned').add(eventRecord);
                        source.crudManager.sync();
                    }
                }
            }
        },

        taskEditFeature : {
            editorConfig : {
                title : 'Task'
            },

            items : {
                generalTab : {
                    items : {
                        resourcesField : {
                            label : 'Technician'
                        },
                        effortField  : false,
                        vehicleField : {
                            type   : 'text',
                            name   : 'licensePlate',
                            label  : 'Vehicle',
                            weight : 150
                        },
                        // Store for this field is set inside a useEffect hook once data is loaded
                        skillField : {
                            type         : 'combo',
                            multiSelect  : true,
                            idField      : 'id',
                            displayField : 'name',
                            label        : 'Skills',
                            name         : 'skills',
                            weight       : 160
                        },
                        manuallyScheduledField: {
                            type   : 'checkbox',
                            label  : 'Manually Scheduled?',
                            name   : 'manuallyScheduled',
                            weight : 600 
                        },
                        percentDoneField: false
                    }
                },
                predecessorsTab : false,
                successorsTab   : false,
                advancedTab     : false,
                notesTab        : false
            }
        },

        eventDragFeature : {
            // Validation method, called as you drag events around in the schedule
            validatorFn({ eventRecords, newResource, startDate }) {
                const
                    task  = eventRecords[0],
                    valid = newResource.canPerformTask(task, startDate);

                return valid;
            }
        },

        tools : {
            scoreLabel : {
                weight : 300,
                type   : 'label',
                text   : '',
            },
            resetButton: {
                weight   : 200,
                type     : 'button',
                text     : 'Reset',
                icon     : 'b-icon b-fa-rotate-right',
                cls      : 'b-transparent',
                tooltip  : 'Resets the data',
                onAction : onReset,
            },
            solveButton: {
                weight   : 100,
                type     : 'button',
                text     : 'Solve',
                ref      : 'solveButton',
                icon     : 'b-icon b-fa-wand-magic-sparkles',
                cls      : 'b-transparent',
                tooltip  : 'Tries to fit the unplanned events into the currently displayed timeframe',
                onAction : () => {
                    onSolve()
                }
            }
        },

        calendarHighlightFeature: {
            calendar : 'resource',
            inflate  : {
                x : -8,
                y : -1
            },
            collectAvailableResources({ scheduler, eventRecords }) {
                return scheduler.resourceStore.query(technician => technician.canPerformTask(eventRecords[0]));
            }
        },

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

        columns: [
            {
                type           : 'resourceInfo',
                text           : 'Staff',
                width          : 300,
                showEventCount : false,
                // Show skills each technician has
                showMeta(resourceRecord) {
                    const
                        { skillNames, role }   = resourceRecord,
                        { startDate, endDate } = this.grid,
                        bookedHours            = resourceRecord.getBookedHours(startDate, endDate),
                        overAllocated          = bookedHours > resourceRecord.hoursPerWeek;

                    return `<ul class="skills">${skillNames.map(skill => `<li>${StringHelper.encodeHtml(skill)}</li>`).join('')}</ul>
                        <div data-btip="${bookedHours}h / ${resourceRecord.hoursPerWeek} allocated"><i class="b-fa ${overAllocated ? 'b-fa-triangle-exclamation' : 'b-fa-clock'}"></i>${bookedHours} / ${resourceRecord.hoursPerWeek}</div>`;
                },
                filterable : {
                    filterField : {
                        triggers : {
                            search : {
                                cls : 'b-icon b-fa-filter'
                            }
                        },
                        placeholder : 'Staff'
                    }
                }
            }
        ],

        eventTooltipFeature : {
            template({ eventRecord }) {
                return `<div class="field"><label>Task</label><span>${StringHelper.encodeHtml(eventRecord.name)}</span></div>
                    <div class="field"><label>Required skills</label><ul class="skills">${eventRecord.requiredSkillNames.map(skill => `<li>${skill}</li>`).join('')}</ul></div>
                    <div class="field"><label>Start</label><span>${DateHelper.format(eventRecord.startDate, 'MMM DD LST')}</span></div>
                    <div class="field"><label>Duration</label><span>${eventRecord.fullDuration}</span></div>
                    <div class="field"><label>Assigned to</label><span>${StringHelper.encodeHtml(eventRecord.resource.name)}</span></div>
                    <div class="field"><label>Manually Assigned</label><span>${eventRecord.manuallyScheduled ? 'Yes' : 'No'}</span></div>
                `;
            }
        },

        eventRenderer({ eventRecord }) {
            return `
                <div>
                    <div class="b-event-header">
                        <div class="b-event-name">${eventRecord.name}</div>
                        <div class="b-event-duration">${eventRecord.fullDuration.toString(true)}</div>
                    </div>
                    <div class="license-plate">
                        <div>Vehicle: ${eventRecord.licensePlate}</div>
                        ${eventRecord.manuallyScheduled ? '<div class="manually-scheduled"><i class="b-fa b-fa-map-pin"></i></div>' : ''}
                    </div>
                </div>
            `
        }
    }
};

const useUnplannedGridConfig = (onAddRandomTasks) => {
    return {
        cls                        : 'b-unplanned-grid',
        hideHeaders                : true,
        disableGridRowModelWarning : true,
        flex                       : '0 0 300px',
        ui                         : 'toolbar',
        title                      : 'Unplanned maintenance',
        emptyText                  : 'No unplanned maintenance',
        selectionMode              : {
            multiSelect : false
        },
        features : {
            stripe : true,
            sort   : 'name'
        },

        tools : { 
            addRandomTasksButton: {
                type : 'button',
                text : '+5',
                tooltip : 'Add 5 random tasks',
                onAction : () => onAddRandomTasks(5)
            },
         },

        columns : [
            {
                flex       : 1,
                field      : 'name',
                cellCls    : 'unscheduledNameCell',
                autoHeight : true,
                htmlEncode : false,
                renderer   : ({ record : task }) => `
                        <div class="vehicle-ct">
                            <i class="${StringHelper.encodeHtml(task.iconCls) || ''}"></i>
                            <span class="license-plate">${StringHelper.encodeHtml(task.licensePlate)}</span>
                        </div>
                        <div class="name-container">
                            <div class="main-info"><span class="task-name">${StringHelper.encodeHtml(task.name)}</span></div>
                            <div class="meta-info"><ul class="skills">${task.requiredSkillNames.map(skill => `<li data-btip="This task requires a technician with the following skills: <strong>${task.requiredSkillNames.join(', ')}</strong>">${skill}</li>`).join('')}</ul><span class="duration">${task.duration ? task.duration + 'h' : ''}</span></div>
                        </div>
                    `
            }
        ],
    };
};

export { useSchedulerProConfig, useUnplannedGridConfig };
