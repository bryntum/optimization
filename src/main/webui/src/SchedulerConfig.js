/**
 * Application configuration
 */

const schedulerProps = {
    viewPreset       : 'hourAndDay',
    rowHeight        : 50,
    barMargin        : 5,
    multiEventSelect : true,

    columns : [{ text : 'Name', field : 'name', width : 130 }],

    crudManager : {
        transport : {
            load : {
                url : './api/read'
            }
        },
        autoLoad : true
    }
};

export { schedulerProps };