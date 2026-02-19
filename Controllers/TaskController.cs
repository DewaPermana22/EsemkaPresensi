using LKS_ITSSA_2025.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Identity.Client;

namespace LKS_ITSSA_2025.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class TaskController : ControllerBase
    {
        EsensiAppContext context = new EsensiAppContext();
        [HttpGet]
        public IActionResult TaskName()
        {
            var res = context?.Tasks?.ToList();
            return Ok(res);
        }

        [HttpGet("{id}")]
        public IActionResult TaskDescription(int id)
        {
            var detail = context.TodoTasks?.Where(d => d.TaskId == id).ToList();
            if (detail != null && id != 0)
            {
                return Ok(detail);
            }
            else
            {
                return BadRequest("Todo Task Invalid / not found!");
            }
        }

        [Authorize]
        [HttpGet("me")]
        public IActionResult TaskStatus()
        {
            var idUserString = User.Claims.FirstOrDefault(c => c.Type == "UserID")?.Value;
            if (string.IsNullOrEmpty(idUserString) || !int.TryParse(idUserString, out int idUser))
            {
                return BadRequest("Invalid Token! or Token Not Found!");
            }

            var taskProgresses = context.TaskingProgresses
                .Include(tp => tp.Task)
                .Where(tp => tp.UserId == idUser)
                .ToList();

            if (taskProgresses == null || taskProgresses.Count == 0)
            {
                return NotFound("No tasks found for this user.");
            }

            var taskList = new List<object>();

            foreach (var taskProgress in taskProgresses)
            {
                var getSelesaiTask = context.TodoTasks.Count(d => d.TaskId == taskProgress.TaskId && d.IsSelesai == 2);
                var getBelumTask = context.TodoTasks.Count(d => d.TaskId == taskProgress.TaskId);

                string deadlineFormatted = taskProgress.Deadline?.ToString("dd MMM yyyy") ?? "No Deadline";

                taskList.Add(new
                {
                    TaskID = taskProgress.TaskId,
                    TaskName = taskProgress.Task?.Task1,
                    BelumSelesai = getBelumTask,
                    SudahSelesai = getSelesaiTask,
                    Deadline = deadlineFormatted
                });
            }

            return Ok(taskList);
        }


        [Authorize]
        [HttpGet("status/me")]
        public IActionResult StatusTask()
        {
            var idUserString = User.Claims.FirstOrDefault(c => c.Type == "UserID")?.Value;
            if (string.IsNullOrEmpty(idUserString) || !int.TryParse(idUserString, out int idUser))
            {
                return BadRequest("Invalid Token! or Token Not Found!");
            }

            var taskProgresses = context.TaskingProgresses
                .Include(tp => tp.Task)
                .Where(tp => tp.UserId == idUser)
                .ToList();

            if (taskProgresses == null || taskProgresses.Count == 0)
            {
                return NotFound("No tasks found for this user.");
            }

            var taskList = new List<object>();

            foreach (var taskProgress in taskProgresses)
            {
                var getSelesaiTask = context.TodoTasks.Count(d => d.TaskId == taskProgress.TaskId && d.IsSelesai == 2);
                var getBelumTask = context.TodoTasks.Count(d => d.TaskId == taskProgress.TaskId && d.IsSelesai == 0);
                var taskiItem = context.TodoTasks.Where(d => d.TaskId == taskProgress.TaskId).ToArray();
                string deadlineFormatted = taskProgress.Deadline?.ToString("dd MMM yyyy") ?? "No Deadline";

                taskList.Add(new
                {
                    TaskID = taskProgress.TaskId,
                    TaskName = taskProgress.Task?.Task1,
                    ItemTask = taskiItem,
                });
            }

            return Ok(taskList);
        }

        [Authorize]
        [HttpPut("update/{id}")]
        public IActionResult updt(int id)
        {
            var idUserString = User.Claims.FirstOrDefault(c => c.Type == "UserID")?.Value;
            if (string.IsNullOrEmpty(idUserString) || !int.TryParse(idUserString, out int idUser))
            {
                return BadRequest("Invalid Token! or Token Not Found!");
            }
            var getTaskItem = context.TodoTasks.Where(c => c.Id == id && idUser != 0).FirstOrDefault();
            if (getTaskItem != null)
            {
                getTaskItem.IsSelesai = 1;
                getTaskItem.TanggalSelesai = DateOnly.FromDateTime(DateTime.Now);
                context.TodoTasks.Update(getTaskItem);
                context.SaveChanges();
                return Ok(getTaskItem);
            } else
            {
                return BadRequest("Id Todo Not Found!");
            }

        }

        [HttpGet("Rekap/me")]
        public IActionResult getRekapTugas()
        {
            var idUserString = User.Claims.FirstOrDefault(c => c.Type == "UserID")?.Value;
            if (string.IsNullOrEmpty(idUserString) || !int.TryParse(idUserString, out int idUser))
            {
                return BadRequest("Invalid Token! or Token Not Found!");
            }

            var today = DateOnly.FromDateTime(DateTime.Now);

            var rekap = (from tp in context.TaskingProgresses
                         join td in context.TodoTasks on tp.TaskId equals td.TaskId
                         where tp.UserId == idUser
                         select new
                         {
                             Status = td.IsSelesai == 2
                                 ? (td.TanggalSelesai > tp.Deadline ? "Terlambat" : "SudahSelesai")
                                 : (td.IsSelesai == 0 && tp.Deadline < today ? "TidakSelesai" : null)
                         })
                         .Where(x => x.Status != null)
                         .GroupBy(x => x.Status)
                         .Select(g => new { Status = g.Key, Jumlah = g.Count() })
                         .ToList();

            return Ok(new
            {
                SudahSelesai = rekap.FirstOrDefault(x => x.Status == "SudahSelesai")?.Jumlah ?? 0,
                Terlambat = rekap.FirstOrDefault(x => x.Status == "Terlambat")?.Jumlah ?? 0,
                TidakSelesai = rekap.FirstOrDefault(x => x.Status == "TidakSelesai")?.Jumlah ?? 0
            });
        }

    }
}


//eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJVc2VySUQiOjMsImV4cCI6MTc3MDcyOTA2N30.B - 2NcTZE7dYr1pVu4HWspuG1DHWsmg3j7Sio6N4bFpY