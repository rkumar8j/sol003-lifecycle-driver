function addProperty(message, propertyName, propertyValue) {
    var propertyNameParts = propertyName.split('.');
    var messageObject = message;
    for (var i = 0; i < propertyNameParts.length; i++) {
        var propertyNamePart = propertyNameParts[i]
        if (!messageObject.hasOwnProperty(propertyNamePart) && i < (propertyNameParts.length - 1)) {
            if (isNaN(propertyNameParts[i+1])) {
                messageObject[propertyNamePart] = {};
            } else {
                messageObject[propertyNamePart] = [];
            }
        } else if (i == (propertyNameParts.length - 1)) {
            messageObject[propertyNamePart] = propertyValue;
        }
        messageObject = messageObject[propertyNamePart];
    }
}

function setPropertyIfNotNull(sourceMap, targetObject, propertyName) {
    if (sourceMap[propertyName] !== null) {
        targetObject[propertyName] = sourceMap[propertyName];
    }
}

function flattenPropertyMap(map) {
    var returnMap = {};
    _flattenPropertyMap(returnMap, map, '');
    return returnMap;
}

function _flattenPropertyMap(returnMap, map, prefix) {
    for (var propertyName in map) {
        var propertyValue = map[propertyName];
        //print ('Property [' + prefix + propertyName + '] is of type ' + typeof propertyValue + ', and has value [' + propertyValue + ']');
        if (typeof propertyValue === 'object') {
            _flattenPropertyMap(returnMap, propertyValue, prefix + propertyName + '.');
        } else if (typeof propertyValue === 'array') {
            _flattenPropertyArray(returnMap, propertyValue, prefix + propertyName + '.');
        } else if (propertyValue) {
            returnMap[prefix + propertyName] = propertyValue;
        }
    }
}

function _flattenPropertyArray(returnMap, array, prefix) {
    for (var i = 0; i < array.length; i++) {
        var propertyValue = array[i];
        //print ('Property (' + prefix + '[' + i + ']) is of type ' + typeof propertyValue + ', and has value [' + propertyValue + ']');
        if (typeof propertyValue === 'object') {
            _flattenPropertyMap(returnMap, propertyValue, prefix + i + '.');
        } else if (typeof propertyValue === 'array') {
            _flattenPropertyArray(returnMap, propertyValue, prefix + i + '.');
        } else if (propertyValue) {
            returnMap[prefix + i] = propertyValue;
        }
    }
}