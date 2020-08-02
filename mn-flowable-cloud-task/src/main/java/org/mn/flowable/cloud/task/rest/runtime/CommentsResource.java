/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mn.flowable.cloud.task.rest.runtime;

import org.mn.flowable.cloud.common.model.ResultListDataRepresentation;
import org.mn.flowable.cloud.task.model.runtime.CommentRepresentation;
import org.mn.flowable.cloud.task.service.runtime.FlowableCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * REST resource related to comment collection on tasks and process instances.
 *
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@RestController
@RequestMapping("/app")
public class CommentsResource {

    @Autowired
    protected FlowableCommentService commentService;

    @GetMapping(value = "/rest/tasks/{taskId}/comments", produces = "application/json")
    public ResultListDataRepresentation getTaskComments(@PathVariable("taskId") String taskId) {
        return commentService.getTaskComments(taskId);
    }

    @PostMapping(value = "/rest/tasks/{taskId}/comments", produces = "application/json")
    public CommentRepresentation addTaskComment(@RequestBody CommentRepresentation commentRequest, @PathVariable("taskId") String taskId) {
        return commentService.addTaskComment(commentRequest, taskId);
    }

    @GetMapping(value = "/rest/process-instances/{processInstanceId}/comments", produces = "application/json")
    public ResultListDataRepresentation getProcessInstanceComments(@PathVariable("processInstanceId") String processInstanceId) {
        return commentService.getProcessInstanceComments(processInstanceId);
    }

    @PostMapping(value = "/rest/process-instances/{processInstanceId}/comments", produces = "application/json")
    public CommentRepresentation addProcessInstanceComment(@RequestBody CommentRepresentation commentRequest,
                                                           @PathVariable("processInstanceId") String processInstanceId) {
        return commentService.addProcessInstanceComment(commentRequest, processInstanceId);
    }

}
