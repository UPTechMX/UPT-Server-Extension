<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

            <!DOCTYPE html>
            <html>
            <!--viewName-->

            <head>
                <title>Geoportal</title>

                <!-- ############# css ################# -->
                <link rel="stylesheet" type="text/css" href="/Oskari${path}/icons.css" />
                <link rel="stylesheet" type="text/css" href="/Oskari${path}/oskari.min.css" />
                <style type="text/css">
                    @media screen {
                        body {
                            margin: 0;
                            padding: 0;
                        }
                        #mapdiv {
                            width: 100%;
                        }
                        #maptools {
                            background-color: #333438;
                            height: 100%;
                            position: absolute;
                            top: 0;
                            width: 153px;
                            z-index: 2;
                        }
                        #contentMap {
                            height: 100%;
                            margin-left: 170px;
                        }
                        #login {
                            margin-left: 5px;
                        }
                        #login input[type="text"],
                        #login input[type="password"] {
                            width: 90%;
                            margin-bottom: 5px;
                            background-image: url("/Oskari/${version}/resources/images/forms/input_shadow.png");
                            background-repeat: no-repeat;
                            padding-left: 5px;
                            padding-right: 5px;
                            border: 1px solid #B7B7B7;
                            border-radius: 4px 4px 4px 4px;
                            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1) inset;
                            color: #878787;
                            font: 13px/100% Arial, sans-serif;
                        }
                        #login input[type="submit"] {
                            width: 90%;
                            margin-bottom: 5px;
                            padding-left: 5px;
                            padding-right: 5px;
                            border: 1px solid #B7B7B7;
                            border-radius: 4px 4px 4px 4px;
                            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1) inset;
                            color: #878787;
                            font: 13px/100% Arial, sans-serif;
                        }
                        #login p.error {
                            font-weight: bold;
                            color: red;
                            margin-bottom: 10px;
                        }
                        #login a {
                            color: #FFF;
                            padding: 5px;
                        }
                        #maptools {
                            padding-top: 4em;
                        }
                        div.mapplugins .mappluginsContainer .mappluginsContent .mapplugin.scalebar {
                            z-index: 1100 !important;
                        }
                        button:not([class*="ant-"]) {
                            background: none;
                        }
                        html body app-root app-tools-sidebar p-button.ui-button-secondary button.toolbarbtn.ui-button.ui-widget.ui-state-default.ui-corner-all.ui-button-icon-only {
                            padding-top: 4em;
                        }
                        html body div#contentMap.oskariui.container-fluid.oskari-map-window-fullscreen div.row-fluid.oskariui-mode-content div.span12.oskariui-center div#mapdiv.olMap div.mapplugins.right.top div.mappluginsContainer {
                            display: none;
                        }
                        html body div#contentMap.oskariui.container-fluid div.row-fluid.oskariui-mode-content {
                            padding-top: -1.5em;
                        }
                        .textarea,
                        button:not(.ui-button),
                        input[type=button]:not([class*=ant-]):not(.ui-button),
                        input[type=email],
                        input[type=file],
                        input[type=number],
                        input[type=password],
                        input[type=reset],
                        input[type=search],
                        input[type=submit],
                        input[type=tel],
                        input[type=text],
                        input[type=url],
                        select,
                        textarea {
                            border-color: #bfbfbf #dedede #dedede #bfbfbf;
                            border-style: solid;
                            border-width: 1px;
                            padding: 8px 5px;
                            font-family: inherit
                        }
                        button:not([class*=ant-]):not(.ui-button)::-moz-focus-inner,
                        input[type=button]:not([class*=ant-]):not(.ui-button)::-moz-focus-inner,
                        input[type=reset]::-moz-focus-inner,
                        input[type=submit]::-moz-focus-inner {
                            padding: 0 !important;
                            border: 0 !important
                        }
                        button:not([class*=ant-]):not(.ui-button),
                        input[type=button]:not([class*=ant-]):not(.ui-button),
                        input[type=reset],
                        input[type=submit] {
                            cursor: pointer;
                            overflow: visible;
                            padding: 8px 5px;
                            line-height: inherit;
                            min-width: 4em;
                            width: auto
                        }
                        button:not([class*=ant-]):not(.ui-button):focus,
                        button:not([class*=ant-]):not(.ui-button):hover:enabled,
                        input[type=button]:not([class*=ant-]):not(.ui-button):focus,
                        input[type=button]:not([class*=ant-]):not(.ui-button):hover:enabled,
                        input[type=reset]:focus,
                        input[type=reset]:hover:enabled,
                        input[type=submit]:focus,
                        input[type=submit]:hover:enabled {
                            background: #dff4ff;
                            border: 1px solid #a7cedf;
                            color: #0091ff
                        }
                        .lfr-actions,
                        .lfr-actions ul,
                        button:not([class*=ant-]):not(.ui-button),
                        input[type=button]:not([class*=ant-]):not(.ui-button),
                        input[type=reset],
                        input[type=submit] {
                            background: #d4d4d4 url(../assets/a7ab273602f74c4386e47dd3df3fd5b7.png) repeat-x 0 0;
                            border-color: #c8c9ca #9e9e9e #9e9e9e #c8c9ca;
                            border-style: solid;
                            border-width: 1px;
                            color: #3c3c3c;
                            font-weight: 700;
                            text-shadow: 1px 1px #fff
                        }
                        .lfr-actions:hover,
                        .lfr-actions:hover ul,
                        button:not([class*=ant-]):not(.ui-button):hover:enabled,
                        input[type=button]:not([class*=ant-]):not(.ui-button):hover:enabled,
                        input[type=reset]:hover:enabled,
                        input[type=submit]:hover:enabled {
                            background: #b9ced9 url(../assets/bdce22df76d879f2eb75ac9330b6acc6.png) repeat-x 0 0;
                            border-color: #627782;
                            color: #369
                        }
                        .lfr-actions:focus,
                        .lfr-actions:focus ul,
                        button:not([class*=ant-]):not(.ui-button):focus,
                        input[type=button]:not([class*=ant-]):not(.ui-button):focus,
                        input[type=reset]:focus,
                        input[type=submit]:focus {
                            background: #ebebeb url(../assets/fb8a01d496378ed855fc52ac14bc6751.png) repeat-x 0 0;
                            border-color: #555
                        }
                        button:not([class*=ant-]):not(.ui-button):disabled,
                        input:disabled,
                        select:disabled,
                        textarea:disabled {
                            cursor: default;
                            opacity: .5
                        }
                        body,
                        button:not(.ui-button),
                        input,
                        select,
                        textarea {
                            font-size: 14px;
                            line-height: 21px
                        }
                        .lfr-actions,
                        .lfr-actions ul,
                        button:not([class*="ant-"]).ui-button,
                        input[type="button"]:not([class*="ant-"]).ui-button,
                        input[type="reset"].ui-button,
                        input[type="submit"].ui-button {
                            background: none !important;
                            border-color: none !important;
                            border-style: none !important;
                            border-width: 0 !important;
                            color: none !important;
                            font-weight: 700 !important;
                            text-shadow: none !important;
                        }
                        button:not([class*="ant-"]).ui-button,
                        input[type="button"]:not([class*="ant-"]).ui-button,
                        input[type="reset"].ui-button,
                        input[type="submit"].ui-button {
                            cursor: pointer !important;
                            overflow: visible !important;
                            padding: 0 !important;
                            line-height: inherit !important;
                            width: auto !important;
                        }
                        .textarea,
                        button.ui-button,
                        input[type="button"]:not([class*="ant-"]).ui-button,
                        input[type="email"].ui-button,
                        input[type="file"].ui-button,
                        input[type="number"].ui-button,
                        input[type="password"].ui-button,
                        input[type="reset"].ui-button,
                        input[type="search"].ui-button,
                        input[type="submit"].ui-button,
                        input[type="tel"].ui-button,
                        input[type="text"].ui-button,
                        input[type="url"],
                        select.ui-button,
                        textarea.ui-button {
                            border-color: none !important;
                            border-style: none !important;
                            border-width: 0 !important;
                            padding: 0 !important;
                            font-family: inherit !important;
                        }
                        .lfr-actions:hover,
                        .lfr-actions:hover ul,
                        button:not([class*="ant-"]).ui-button:hover:enabled,
                        input[type="button"].ui-button:focus:not([class*="ant-"]):hover:enabled,
                        input[type="reset"].ui-button:focus:hover:enabled,
                        input[type="submit"].ui-button:focus:hover:enabled {
                            color: #FFFFFF !important;
                        }
                        .lfr-actions:focus,
                        .lfr-actions:focus ul,
                        button:not([class*=ant-]).ui-button:focus,
                        input[type=button].ui-button:focus:not([class*=ant-]),
                        input[type=reset].ui-button:focus,
                        input[type=submit].ui-button:focus {
                            color: #FFFFFF !important
                        }
                        button:not([class*=ant-]):disabled,
                        input:disabled,
                        select:disabled,
                        textarea:disabled {
                            cursor: default !important;
                            opacity: .5 !important
                        } */
                        app-root table.ui-table-scrollable-body-table,
                        app-root .ui-table-wrapper table,
                        app-root .ui-table-scrollable-header-box,
                        app-root .ui-table-scrollable-header-table {
                            font-size: 12px !important;
                            line-height: 18px !important;
                            margin: 0 !important;
                            width: 100% !important;
                            border-collapse: collapse !important;
                            border-top: 1px solid #3c3c3c !important;
                        }
                        app-root .gp_modal.urbanperformance .nav-item .nav-link {
                            background: #2e2f2f !important;
                            color: #FF8680 !important;
                        }
                        app-root #suitability.gp_modal .nav-item .nav-link,
                        app-root .suitability.gp_modal .nav-item .nav-link {
                            background: #2e2f2f !important;
                            color: #6ab1e2 !important;
                        }
                        app-root .urbanperformance.gp_modal .nav-item .active,
                        app-root .urbanperformance .tab-content .active {
                            background: #4B565F !important;
                            color: #FF8680 !important;
                        }
                        app-root #suitability.gp_modal .nav-item .active,
                        app-root #suitability .tab-content .active,
                        app-root .suitability.gp_modal .nav-item .active,
                        app-root .suitability .tab-content .active {
                            background: #4B565F !important;
                            color: #6ab1e2 !important;
                        }
                        .highlight-results {
                            color: #FF8680 !important;
                        }
                        app-root .tab-content .results-title {
                            color: #C0C4C6 !important;
                        }
                        app-root #suitability .ui-slider .ui-slider-handle {
                            background-color: #FFFFFF;
                            border-color: #FFFFFF;
                        }
                        app-root #suitability .ui-slider:not(.ui-state-disabled) .ui-slider-handle:hover,
                        #suitability .custom-control-input:checked~.custom-control-label::before {
                            background-color: #6ab1e2 !important;
                            border-color: #6ab1e2 !important;
                        }
                        app-root #suitability .ui-slider .ui-slider-range {
                            background-color: #6ab1e2 !important;
                        }
                        .suitability .selected-layers {
                            background-color: #323232;
                            border: 1px solid #191919;
                        }
                        app-root .ui-fieldset-legend {
                            width: 38%;
                            font-size: 1em;
                        }
                        app-root .urbanperformance .ui-fieldset-legend a:hover,
                        app-root .urbanperformance .ui-table .ui-sortable-column.ui-state-highlight,
                        app-root .urbanperformance .ui-table .ui-table-tbody>tr.ui-state-highlight,
                        app-root .urbanperformance .ui-paginator-page.ui-state-active,
                        app-root .urbanperformance .ui-progressbar .ui-progressbar-value {
                            background-color: #FF8680 !important;
                        }
                        app-root .suitability .ui-fieldset-legend a:hover,
                        app-root .suitability .ui-table .ui-sortable-column.ui-state-highlight,
                        app-root .suitability .ui-table .ui-table-tbody>tr.ui-state-highlight,
                        app-root .suitability .ui-paginator-page.ui-state-active,
                        app-root .suitability .ui-progressbar .ui-progressbar-value {
                            background-color: #47A599 !important;
                        }
                        app-root .suitability .ui-table.settingsTable tr.ui-state-highlight {
                            background-color: #323232 !important;
                        }
                        app-root .ui-fieldset-legend a,
                        app-root .ui-fieldset-legend .ui-fieldset-toggler {
                            color: #FFFFFF !important;
                        }
                        app-root .ui-fieldset-legend a:hover,
                        app-root .ui-fieldset-legend .ui-fieldset-toggler:hover,
                        app-root .ui-fieldset-legend a:enabled,
                        app-root .ui-fieldset-legend .ui-fieldset-toggler:a {
                            color: #FFFFFF !important;
                        }
                        app-root .ui-fieldset {
                            background-color: transparent !important;
                        }
                        app-root .urbanperformance .ui-button,
                        .urbanperformance.calculate,
                        app-root .urbanperformance .ui-chkbox-box.ui-state-active {
                            background-color: #FF8680 !important;
                            border-color: #FF8680 !important;
                            color: #FFFFFF !important;
                        }
                        app-root .suitability .ui-button,
                        app-root .suitability .ui-chkbox-box.ui-state-active {
                            background-color: #47A599 !important;
                            border-color: #47A599 !important;
                            color: #FFFFFF !important;
                        }
                        app-root .urbanperformance .ui-inputtext:hover,
                        app-root .urbanperformance .ui-inputtext:focus,
                        app-root .urbanperformance .ui-multiselect:hover,
                        app-root .urbanperformance .ui-multiselect:focus,
                        app-root .urbanperformance .ui-multiselect:active,
                        app-root .urbanperformance .ui-multiselect.ui-state-focus,
                        app-root .urbanperformance .ui-dropdown:hover,
                        app-root .urbanperformance .ui-dropdown:focus,
                        app-root .urbanperformance .ui-dropdown:active,
                        app-root .urbanperformance .ui-dropdown.ui-state-focus,
                        app-root .urbanperformance .ui-chkbox-box:hover,
                        app-root .urbanperformance .ui-chkbox-box:focus,
                        app-root .urbanperformance .ui-chkbox-box:active {
                            border-color: #FF8680 !important;
                        }
                        app-root .urbanperformance .ui-inputgroup .ui-multiselect {
                            display: inline-flex !important;
                        }
                        app-root .urbanperformance .ui-multiselect-item.ui-state-highlight,
                        app-root .urbanperformance .ui-dropdown-item.ui-state-highlight,
                        app-root .urbanperformance .ui-treenode-label.ui-state-highlight {
                            background-color: #FF8680 !important;
                        }
                        app-root .suitability .ui-multiselect-item.ui-state-highlight,
                        app-root .suitability .ui-dropdown-item.ui-state-highlight,
                        app-root .suitability .ui-treenode-label.ui-state-highlight {
                            background-color: #47A599 !important;
                        }
                        app-root .suitability .ui-button,
                        app-root .suitability .ui-chkbox-box.ui-state-active {
                            background-color: #47A599 !important;
                            border-color: #47A599 !important;
                            text-decoration: none;
                        }
                        app-root .suitability .ui-inputswitch-checked .ui-inputswitch-slider {
                            background-color: #47A599 !important;
                        }
                        app-root .suitability .ui-inputtext:hover,
                        app-root .suitability .ui-inputtext:focus,
                        app-root .suitability .ui-multiselect:hover,
                        app-root .suitability .ui-multiselect:focus,
                        app-root .suitability .ui-multiselect:active,
                        app-root .suitability .ui-multiselect.ui-state-focus,
                        app-root .suitability .ui-dropdown:hover,
                        app-root .suitability .ui-dropdown:focus,
                        app-root .suitability .ui-dropdown:active,
                        app-root .suitability .ui-dropdown.ui-state-focus,
                        app-root .suitability .ui-chkbox-box:hover,
                        app-root .suitability .ui-chkbox-box:focus,
                        app-root .suitability .ui-chkbox-box:active {
                            border-color: #47A599 !important;
                        }
                        app-root .suitability .ui-chkbox-box.ui-state-focus,
                        app-root .suitability .ui-button:enabled:focus {
                            box-shadow: 0 0 0 0.2em #47A599 !important;
                        }
                        app-root .urbanperformance .ui-chkbox-box.ui-state-focus,
                        app-root .urbanperformance .ui-button:enabled:focus {
                            box-shadow: 0 0 0 0.2em #FF8680 !important;
                        }
                        app-root .suitability .ui-dropdown-filter-icon,
                        app-root .suitability .ui-multiselect-filter-icon {
                            color: #47A599 !important;
                        }
                        app-root .geoportal-btn.suitability:not(.calculateUP) .ui-button-icon-left,
                        app-root .geoportal-btn.urbanperformance:not(.calculateUP) .ui-button-icon-left,
                        app-root .geoportal-btn.tools-geoportal:not(.geoportal-title-btn) .ui-button-icon-left {
                            font-size: 2em !important;
                        }
                        app-root .geoportal-btn.tools-geoportal .ui-button:enabled:focus {
                            box-shadow: 0 0 0 0.2em #4E565E !important;
                        }
                        app-root .geoportal-btn.urbanperformance .ui-button,
                        app-root .geoportal-btn.urbanperformance .ui-button:hover,
                        app-root .geoportal-btn.urbanperformance .ui-button:focus {
                            background-color: #FF8680 !important;
                            min-width: 2.5em !important;
                            border: #FF8680 !important;
                        }
                        app-root .geoportal-btn.suitability .ui-button,
                        app-root .geoportal-btn.suitability .ui-button:hover,
                        app-root .geoportal-btn.suitability .ui-button:focus {
                            background-color: #47A599 !important;
                            border: #47A599 !important;
                            min-width: 2.5em !important;
                        }
                        app-root .geoportal-btn.advanced-geoportal .ui-button,
                        app-root .geoportal-btn.advanced-geoportal .ui-button:hover,
                        app-root .geoportal-btn.advanced-geoportal .ui-button:focus {
                            background-color: #2e2f2f !important;
                            border: 1px solid #2e2f2f !important;
                        }
                        app-root .geoportal-btn.tools-geoportal .ui-button,
                        app-root .geoportal-btn.tools-geoportal.ui-button {
                            background-color: #d9d9d9!important;
                            border: 1px solid #d9d9d9!important;
                            color: #ffffff!important;
                            min-width: 2.5em !important;
                        }
                        app-root .geoportal-btn.tools-geoportal .ui-button:hover,
                        app-root .geoportal-btn.tools-geoportal .ui-button:focus,
                        app-root .geoportal-btn.tools-geoportal.ui-button:hover,
                        app-root .geoportal-btn.tools-geoportal.ui-button:focus {
                            background-color: #47A599!important;
                            border: 1px solid #47A599!important;
                            color: #ffffff!important;
                        }
                        app-root .ui-button.ui-state-default.ui-button-warning,
                        app-root .ui-button.ui-state-default.ui-button-warning:hover,
                        app-root .ui-button.ui-state-default.ui-button-warning:focus,
                        app-root .ui-buttonset.ui-button-warning>.ui-button.ui-state-default,
                        app-root .ui-buttonset.ui-button-warning>.ui-button.ui-state-default:hover,
                        app-root .ui-buttonset.ui-button-warning>.ui-button.ui-state-default:focus {
                            color: #333 !important;
                            background-color: #ffba01 !important;
                            border: 1px solid#ffba01 !important;
                            min-width: 2.5em !important;
                        }
                        app-root .toolbarbtn.ui-button {
                            background-color: #323232 !important;
                            color: #FFFFFF !important;
                            border: none !important;
                        }
                        app-root .urbanperformance .ui-dropdown-filter-icon,
                        app-root .urbanperformance .ui-multiselect-filter-icon {
                            color: #FF8680 !important;
                        }
                        app-root #suitability .ui-spinner-button {
                            background-color: #47A599 !important;
                            border-color: #47A599 !important;
                        }
                        app-root .urbanperformance .ui-spinner-button,
                        app-root #urbanperformance .ui-spinner-button {
                            background-color: #FF8680 !important;
                            border-color: #FF8680 !important;
                        }
                        app-root .ui-spinner-button {
                            min-width: 1.5em !important;
                        }
                        app-root .geoportal-btn.tools-geoportal .ui-button:enabled:focus,
                        app-root .geoportal-btn.advanced-geoportal .ui-button:enabled:focus {
                            box-shadow: 0 0 0 0.2em #2e2f2f !important;
                        }
                        app-root .geoportal-btn.tools-geoportal .ui-button,
                        app-root .geoportal-btn.tools-geoportal .ui-button:hover,
                        app-root .geoportal-btn.tools-geoportal .ui-button:focus,
                        app-root .geoportal-btn.advanced-geoportal .ui-button,
                        app-root .geoportal-btn.advanced-geoportal .ui-button:hover,
                        app-root .geoportal-btn.advanced-geoportal .ui-button:focus {
                            background-color: #2e2f2f !important;
                            border: #2e2f2f !important;
                        }
                        app-root .ui-button.ui-state-default.ui-button-success,
                        app-root .ui-buttonset.ui-button-success>.ui-button.ui-state-default {
                            color: #fff !important;
                            background-color: #34a835 !important;
                            border: 1px solid #34a835 !important;
                        }
                        app-root .ui-button.ui-state-default.ui-button-success:enabled:hover,
                        app-root .ui-buttonset.ui-button-success>.ui-button.ui-state-default:enabled:hover {
                            background-color: #107d11 !important;
                            color: #fff !important;
                            border-color: #107d11 !important;
                        }
                        app-root .ui-button.ui-state-default.ui-button-success:enabled:active,
                        app-root .ui-buttonset.ui-button-success>.ui-button.ui-state-default:enabled:active {
                            background-color: #0c6b0d !important;
                            color: #fff !important;
                            border-color: #0c6b0d !important;
                        }
                        app-root .ui-button.ui-state-default.ui-button-secondary,
                        app-root .ui-buttonset.ui-button-secondary>.ui-button.ui-state-default {
                            color: #333 !important;
                            background-color: #f4f4f4 !important;
                            border: 1px solid #f4f4f4 !important;
                        }
                        app-root .ui-button.ui-state-default.ui-button-secondary:enabled:hover,
                        app-root .ui-buttonset.ui-button-secondary>.ui-button.ui-state-default:enabled:hover {
                            background-color: #c8c8c8 !important;
                            color: #333 !important;
                            border-color: #c8c8c8 !important;
                        }
                        app-root .ui-button.ui-state-default.ui-button-secondary:enabled:focus,
                        app-root .ui-buttonset.ui-button-secondary>.ui-button.ui-state-default:enabled:focus {
                            box-shadow: 0 0 0 .2em #f1b6c8 !important;
                        }
                        app-root .ui-button.ui-state-default.ui-button-secondary:enabled:active,
                        app-root .ui-buttonset.ui-button-secondary>.ui-button.ui-state-default:enabled:active {
                            background-color: #a0a0a0 !important;
                            color: #333 !important;
                            border-color: #a0a0a0 !important;
                        }
                        app-root .btn.disabled,
                        app-root .btn:disabled,
                        app-root .ui-state-disabled,
                        app-root .ui-state-disabled:disabled,
                        app-root .ui-button:disabled {
                            opacity: 0.15 !important;
                        }
                        app-root .geoportal-dialog .ui-inputtext:hover,
                        app-root .geoportal-dialog .ui-inputtext:focus,
                        app-root .geoportal-dialog .ui-multiselect:hover,
                        app-root .geoportal-dialog .ui-multiselect:focus,
                        app-root .geoportal-dialog .ui-multiselect:active,
                        app-root .geoportal-dialog .ui-multiselect.ui-state-focus,
                        app-root .geoportal-dialog .ui-dropdown:hover,
                        app-root .geoportal-dialog .ui-dropdown:focus,
                        app-root .geoportal-dialog .ui-dropdown:active,
                        app-root .geoportal-dialog .ui-dropdown.ui-state-focus,
                        app-root .geoportal-dialog .ui-chkbox-box:hover,
                        app-root .geoportal-dialog .ui-chkbox-box:focus,
                        app-root .geoportal-dialog .ui-chkbox-box:active {
                            border-color: #7e9baf !important;
                        }
                        app-root .geoportal-dialog .ui-button,
                        app-root .geoportal-dialog .ui-button:hover,
                        app-root .geoportal-dialog .ui-button:focus {
                            background-color: #7e9baf !important;
                            min-width: 0 !important;
                            border: #7e9baf !important;
                        }
                        app-root .geoportal-dialog .ui-button:enabled:focus {
                            box-shadow: 0 0 0 0.2em #7e9baf !important;
                        }
                        app-root .geoportal-dialog .ui-multiselect-item.ui-state-highlight,
                        app-root .geoportal-dialog .ui-dropdown-item.ui-state-highlight,
                        app-root .geoportal-dialog .ui-treenode-label.ui-state-highlight {
                            background-color: #7e9baf !important;
                        }
                        app-root .geoportal-dialog .ui-button,
                        app-root .geoportal-dialog .ui-chkbox-box.ui-state-active {
                            background-color: #7e9baf !important;
                            border-color: #7e9baf !important;
                        }
                        app-root .suitability .ui-button.uh-delete-btn,
                        app-root .suitability .ui-button:enabled:focus {
                            background-color: #B71C2A !important;
                            border-color: #B71C2A !important;
                        }
                        app-root .suitability .ui-button.uh-save-btn,
                        app-root .suitability .ui-button:enabled:focus,
                        app-root .suitability .ui-button.uh-save-btn:hover,
                        app-root .suitability .ui-button.uh-save-btn:focus {
                            background-color: #47A599!important;
                            border: 1px solid #47A599!important;
                            color: #ffffff!important;
                        }
                        app-root #analysis.suitability.geoportal-btn.ui-button {
                            border: 1px solid #47A599 !important;
                            background-color: #47A599 !important;
                        }

                        app-root #analysis.suitability.geoportal-btn.ui-button:hover,
                        app-root #analysis.suitability.geoportal-btn.ui-button:focus,
                        app-root #analysis.suitability.geoportal-btn.ui-button:active {
                            border: 1px solid #47A599 !important;
                            background-color: #47A599 !important;
                        }

                        .download-basket-tile {
                            display: none !important;
                        }
                    }
                </style>
                <!-- ############# /css ################# -->
                <link rel="stylesheet" href="/Oskari${path}/PLID/styles.css">
                <script>
                    window.__session_active = false;
                </script>
            </head>

            <body>
                <app-root id="pltools"></app-root>
                <nav id="maptools">
                    <div id="logobar">
                    </div>
                    <div id="language-selector-root">
                    </div>
                    <div id="menubar">
                    </div>
                    <div id="divider">
                    </div>
                    <div id="toolbar">
                    </div>
                    <div id="login">
                        <c:choose>
                            <c:when test="${!empty loginState}">
                                <p class="error">
                                    <spring:message code="invalid_password_or_username" text="Invalid password or username!" />
                                </p>
                            </c:when>
                        </c:choose>
                        <c:choose>
                            <%-- If logout url is present - so logout link --%>
                                <c:when test="${!empty _logout_uri}">
                                    <form action="${pageContext.request.contextPath}${_logout_uri}" method="POST" id="logoutform">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                        <a href="${pageContext.request.contextPath}${_logout_uri}" style="color: #ffffff;" onClick="jQuery('#logoutform').submit();return false;">
                                            <spring:message code="logout" text="Logout" />
                                        </a>
                                    </form>
                                    <%-- oskari-profile-link id is used by the personaldata bundle - do not modify --%>
                                        <a href="${pageContext.request.contextPath}${_registration_uri}" style="color: #ffffff;" id="oskari-profile-link">
                                            <spring:message code="account" text="Account" />
                                        </a>
                                        <script>
                                            window.__session_active = true;
                                        </script>
                                </c:when>
                                <%-- Otherwise show appropriate logins --%>
                                    <c:otherwise>
                                        <c:if test="${!empty _login_uri && !empty _login_field_user}">
                                            <form action='${pageContext.request.contextPath}${_login_uri}' method="post" accept-charset="UTF-8">
                                                <input size="16" id="username" name="${_login_field_user}" type="text" placeholder="<spring:message code=" username " text=" Username " />" autofocus required>
                                                <input size="16" id="password" name="${_login_field_pass}" type="password" placeholder="<spring:message code=" password " text=" Password " />" required>
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                <input type="submit" id="submit" value="<spring:message code=" login " text=" Log in " />">
                                            </form>
                                        </c:if>
                                        <script>
                                            window.__session_active = false;
                                        </script>
                                    </c:otherwise>
                        </c:choose>
                    </div>


                    <div id="demolink">
                        <a href="#" style="margin: 10px; color: #ffd400;" onClick="changeAppsetup()">EPSG:3067</a>
                    </div>
                </nav>
                <div id="contentMap" class="oskariui container-fluid">
                    <div id="menutoolbar" class="container-fluid"></div>
                    <div class="row-fluid oskariui-mode-content" style="height: 100%; background-color:white;">
                        <div class="oskariui-left"></div>
                        <div class="span12 oskariui-center" style="height: 100%; margin: 0;">
                            <div id="mapdiv"></div>
                        </div>
                        <div class="oskari-closed oskariui-right">
                            <div id="mapdivB"></div>
                        </div>
                    </div>
                </div>


                <!-- ############# Javascript ################# -->

                <script>
                    function changeAppsetup() {
                        var appsetup = Oskari.app.getSystemDefaultViews().filter(function(appsetup) {
                            return appsetup.uuid !== Oskari.app.getUuid();
                        });

                        window.location = window.location.pathname + '?uuid=' + appsetup[0].uuid;
                        return false;
                    }
                </script>
                <!--  OSKARI -->

                <script type="text/javascript">
                    var ajaxUrl = '${ajaxUrl}';
                    var controlParams = ${controlParams};
                </script>
                <%-- Pre-compiled application JS, empty unless created by build job --%>
                    <script type="text/javascript" src="/Oskari${path}/oskari.min.js">
                    </script>
                    <%--language files --%>
                        <script type="text/javascript" src="/Oskari${path}/oskari_lang_${language}.js">
                        </script>

                        <script type="text/javascript" src="/Oskari${path}/index.js">
                        </script>

                        <!-- ############# /Javascript ################# -->
                        <script src="/Oskari${path}/PLID/runtime-es2015.js" type="module"></script>
                        <script src="/Oskari${path}/PLID/runtime-es5.js" nomodule defer></script>
                        <script src="/Oskari${path}/PLID/polyfills-es5.js" nomodule defer></script>
                        <script src="/Oskari${path}/PLID/polyfills-es2015.js" type="module"></script>
                        <script src="/Oskari${path}/PLID/main-es2015.js" type="module"></script>
                        <script src="/Oskari${path}/PLID/main-es5.js" nomodule defer></script>
            </body>

            </html>