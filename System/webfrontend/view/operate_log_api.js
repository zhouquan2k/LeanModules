import CrudApi from '@/utils/api_base';
import { request } from '@/utils/utils'

const operateLogApi = new CrudApi('/operatelog')

export default operateLogApi;
export { operateLogApi };

