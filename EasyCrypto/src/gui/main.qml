import QtQuick 2.9
import QtQuick.Window 2.2
import QtQuick.Controls 2.0

Window {
    id: window
    visible: true
    width: 1024
    height: 512
    title: qsTr("EasyCrypto")

    Column {
        id: website
        x: 10
        y: 10
        width: 231
        height: 275

        ListView {
            id: websiteListView
            orientation: ListView.Vertical
            flickableDirection: Flickable.VerticalFlick
            anchors.fill: parent
            model: websiteModel
            focus: true
            highlight: Rectangle { color: "lightsteelblue";}
            highlightFollowsCurrentItem: true
            objectName: "websiteListView"

            delegate: Component {
                Item {
                    property variant itemData: model.modelData
                    width: parent.width
                    height: 20

                    Row {
                        id: websiteRow
                        spacing: 10
                        anchors.fill: parent

                        Text {
                            text: name
                            font.bold: true
                            anchors.verticalCenter: parent.verticalCenter
                        }

                        MouseArea {
                            id: websiteMouseArea
                            anchors.fill: parent
                            onClicked: {
                                websiteListView.currentIndex = index
                            }
                        }
                    }
                }
            }
            onCurrentIndexChanged: {
                websiteSlot.notifyIndexChanged(currentIndex)
            }

        }
    }

    Column {
        id: action
        x: 397
        y: 12
        width: 231
        height: 275

        ListView {
            id: actionListView
            objectName: "actionListView"
            anchors.fill: parent
            model: actionModel
            orientation: ListView.Vertical
            flickableDirection: Flickable.VerticalFlick
            focus: true
            highlight: Rectangle { color: "lightsteelblue";}
            highlightFollowsCurrentItem: true

            delegate: Component {
                Item {
                    property variant itemData: model.modelData
                    width: parent.width
                    height: 20

                    Row {
                        id: actionRow
                        spacing: 10
                        anchors.fill: parent

                        Text {
                            text: address
                            font.bold: true
                            anchors.verticalCenter: parent.verticalCenter
                        }

                        MouseArea {
                            id: actionMouseArea
                            anchors.fill: parent
                            onClicked: {
                                actionListView.currentIndex = index
                            }
                        }
                    }
                }
            }
            onCurrentIndexChanged: {
                actionSlot.notifyIndexChanged(currentIndex)
            }
        }
    }

    Column {
        id: parameter
        x: 708
        y: 12
        width: 293
        height: 275

        Rectangle {
            id: rectangle
            width: 200
            height: 30
            color: "#a7a2a2"
            anchors.right: parent.right
            anchors.rightMargin: 0
            anchors.left: parent.left
            anchors.leftMargin: 0
            anchors.top: parent.top
            anchors.topMargin: 0
        }

        Rectangle {
            id: rectangle1
            color: "#dad7d7"
            anchors.fill: parent
            anchors.topMargin: 30

            ListView {
                id: parameterListView
                objectName: "parameterListView"
                anchors.fill: parent
                model: parameterModel
                orientation: ListView.Vertical
                flickableDirection: Flickable.VerticalFlick
                focus: true
                highlight: Rectangle { color: "lightsteelblue";}
                highlightFollowsCurrentItem: true

                delegate: Component {
                    Item {
                        property variant itemData: model.modelData
                        width: parent.width
                        height: 20

                        Row {
                            id: parameterRow
                            spacing: 10
                            anchors.fill: parent

                            Text {
                                text: name
                                font.bold: true
                                anchors.verticalCenter: parent.verticalCenter
                            }

                            MouseArea {
                                id: actionMouseArea
                                anchors.fill: parent


                                onClicked: {
                                    parameterListView.currentIndex = index
                                }
                            }

                            TextInput {
                                id: input
                                color: "#151515"; selectionColor: "green"
                                font.pixelSize: 16; font.bold: true
                                maximumLength: 16
                                anchors.centerIn: parent
                                text: "text"
                                focus: true

                                onTextChanged: {
                                    parameterSlot.notifyInputChanged(parameterListView.currentIndex, text)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Column {
        id: log
        x: 11
        y: 312
        width: 813
        height: 155
    }

    Column {
        id: button
        x: 854
        y: 312
        width: 147
        height: 155
        anchors.right: parent.right
        anchors.rightMargin: 23
        anchors.bottom: parent.bottom
        anchors.bottomMargin: 45

        Text {
            id: refreshText
            width: 147
            height: 37
            color: "#ffffff"
            text: qsTr("Refresh")
            elide: Text.ElideMiddle
            anchors.verticalCenter: parent.verticalCenter
            z: 2
            font.family: "Verdana"
            horizontalAlignment: Text.AlignHCenter
            textFormat: Text.AutoText
            styleColor: "#000000"
            verticalAlignment: Text.AlignVCenter
            font.pixelSize: 11

            Rectangle {
                id: refreshButton
                x: 0
                y: 45
                width: 147
                height: 37
                color: "#3787ed"
                radius: 10
                anchors.verticalCenter: parent.verticalCenter
                z: -2

                MouseArea {
                    id: refreshMouseArea
                    anchors.fill: parent
                    objectName: "refreshMouseArea"
                    signal refreshEvent
                    onClicked: refreshEvent()
                }
            }
        }

        Text {
            id: executeText
            width: 147
            height: 37
            color: "#ffffff"
            text: qsTr("Execute")
            anchors.top: parent.top
            anchors.topMargin: 0
            z: 1
            styleColor: "#000000"
            font.family: "Verdana"
            horizontalAlignment: Text.AlignHCenter
            verticalAlignment: Text.AlignVCenter
            textFormat: Text.AutoText
            font.pixelSize: 11

            Rectangle {
                id: executeButton
                x: 1
                width: 147
                height: 37
                color: "#3787ed"
                radius: 10
                anchors.top: parent.top
                anchors.topMargin: 0
                z: -1

                MouseArea {
                    id: executeMouseArea
                    anchors.fill: parent
                    objectName: "executeMouseArea"
                    signal executeEvent
                    onClicked: executeEvent()
                }
            }
        }

        Text {
            id: quitText
            width: 147
            height: 37
            color: "#ffffff"
            text: qsTr("Quit")
            anchors.bottom: parent.bottom
            anchors.bottomMargin: 0
            font.family: "Verdana"
            z: 1
            textFormat: Text.AutoText
            horizontalAlignment: Text.AlignHCenter
            styleColor: "#000000"
            verticalAlignment: Text.AlignVCenter
            font.pixelSize: 11

            Rectangle {
                id: quitButton
                x: 0
                y: -111
                width: 147
                height: 37
                color: "#3787ed"
                radius: 10
                anchors.bottom: parent.bottom
                anchors.bottomMargin: 0
                z: -1

                MouseArea {
                    id: quitMouseArea
                    objectName: "quitMouseArea"
                    anchors.fill: parent
                }
            }
        }





    }

    Connections {
        target: quitMouseArea
        onClicked: window.close()
    }
}
