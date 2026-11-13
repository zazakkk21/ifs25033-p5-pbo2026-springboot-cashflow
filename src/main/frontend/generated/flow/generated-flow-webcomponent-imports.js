import { injectGlobalWebcomponentCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { injectGlobalCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { css, unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin';
import $cssFromFile_0 from '@vaadin/vaadin-lumo-styles/lumo.css?inline';
import '@vaadin/vertical-layout/src/vaadin-vertical-layout.js';
import '@vaadin/app-layout/src/vaadin-app-layout.js';
import '@vaadin/avatar/src/vaadin-avatar.js';
import '@vaadin/button/src/vaadin-button.js';
import '@vaadin/tooltip/src/vaadin-tooltip.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/horizontal-layout/src/vaadin-horizontal-layout.js';
import '@vaadin/grid/src/vaadin-grid.js';
import '@vaadin/grid/src/vaadin-grid-column.js';
import '@vaadin/grid/src/vaadin-grid-sorter.js';
import '@vaadin/checkbox/src/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import 'Frontend/generated/jar-resources/flow-component-directive.js';
import 'lit';
import 'Frontend/generated/jar-resources/vaadin-grid/gridConnector.ts';
import '@vaadin/component-base/src/debounce.js';
import '@vaadin/component-base/src/async.js';
import '@vaadin/component-base/src/dom-utils.js';
import '@vaadin/grid/src/vaadin-grid-active-item-mixin.js';
import 'Frontend/generated/jar-resources/vaadin-grid/vaadin-grid-flow-selection-column.ts';
import '@vaadin/grid/src/vaadin-grid-data-provider-mixin.js';
import '@vaadin/grid/src/vaadin-grid-mixin.js';
import '@vaadin/grid/src/vaadin-grid-column-group.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import 'lit/directives/live.js';
import '@vaadin/context-menu/src/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/vaadin-context-menu/contextMenuConnector.ts';
import 'Frontend/generated/jar-resources/vaadin-context-menu/contextMenuTargetConnector.ts';
import '@vaadin/component-base/src/gestures.js';
import '@vaadin/text-field/src/vaadin-text-field.js';
import '@vaadin/combo-box/src/vaadin-combo-box.js';
import 'Frontend/generated/jar-resources/vaadin-combo-box/comboBoxConnector.ts';
import '@vaadin/combo-box/src/vaadin-combo-box-placeholder.js';
import '@vaadin/combo-box/src/vaadin-combo-box-data-provider-mixin.js';
import '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box.js';
import '@vaadin/dialog/src/vaadin-dialog.js';
import '@vaadin/text-area/src/vaadin-text-area.js';
import '@vaadin/upload/src/vaadin-upload.js';
import 'Frontend/generated/jar-resources/vaadin-upload/uploadManagerConnector.ts';
import '@vaadin/upload/vaadin-upload-manager.js';
import '@vaadin/confirm-dialog/src/vaadin-confirm-dialog.js';
import '@vaadin/notification/src/vaadin-notification.js';
import '@vaadin/password-field/src/vaadin-password-field.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';
import 'react-router';
import 'react';

injectGlobalWebcomponentCss($cssFromFile_0.toString());
const loadOnDemand = (key) => {
  const pending = [];
  if (key === '180440310387636ff60499b98cbad7afbccb61da322cb5b592bbff4922ab98d3') {
    pending.push(import('./chunks/chunk-3076be9f31fdbb7020c1d2625511512c89f5f171853442171aca610f6a505700.js'));
  }
  if (key === '9f73e1f390f669c2f9b216f246fa87e45423b9d569d05da4fe796f3b5c723d21') {
    pending.push(import('./chunks/chunk-3076be9f31fdbb7020c1d2625511512c89f5f171853442171aca610f6a505700.js'));
  }
  if (key === '8932590679cd810ca186a613c2fbceeceadfb2123a6844d59a26dd07997f8e0f') {
    pending.push(import('./chunks/chunk-3076be9f31fdbb7020c1d2625511512c89f5f171853442171aca610f6a505700.js'));
  }
  if (key === 'd02ffa227ca744b3d316c56062ed0e201f74eca2f0ae8979672f3dfe1dc09d30') {
    pending.push(import('./chunks/chunk-9dd274906f49628d8bbdc4cfede3b3de8f0bab64bced0a1b6df6db55ff79d13f.js'));
  }
  return Promise.all(pending);
}
window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}