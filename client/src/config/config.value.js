import { keys } from './config';

const LOCAL = 'LOCAL';
const DEV = 'DEV';
const STAGE = 'STAGE';
const PROD = 'PROD';
const DOCKER = 'DOCKER';
const K8S = 'K8S';
const GCP = 'GCP';
let IP = '127.0.0.1';
// IP = '192.168.1.101'; // peja

export const configValue = (_target, _env) => {
  let result;
  switch (_target) {
    case keys.SERVER_POINT:
      if (_env === LOCAL) result = `http://localhost`;
      else if (_env === DEV) result = `wss://sample.com/`;
      else if (_env === STAGE) result = `wss://sample/`;
      else if (_env === PROD) result = `wss://sample/`;
      else if (_env === K8S) result = `http://localhost`;
      else if (_env === GCP) result = `http://34.10.181.143`;
      break;
    case keys.API_BASE_PORT:
      if (_env === LOCAL) result = `8080`;
      else if (_env === DOCKER) result = `8081`;
      else if (_env === K8S) result = `30001`;
      else if (_env === GCP) result = `30001`;
    default:
      break;
  }
  return result;
};

export default configValue;
