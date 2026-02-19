using System;
using System.Collections.Generic;
using System.Text.Json.Serialization;

namespace LKS_ITSSA_2025.Models;

public partial class Task
{
    public int Id { get; set; }

    public string? Task1 { get; set; }

    [JsonIgnore]
    public virtual ICollection<TaskingProgress> TaskingProgresses { get; set; } = new List<TaskingProgress>();
    [JsonIgnore]
    public virtual ICollection<TodoTask> TodoTasks { get; set; } = new List<TodoTask>();
}
