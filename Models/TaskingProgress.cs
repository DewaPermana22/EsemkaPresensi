using System;
using System.Collections.Generic;

namespace LKS_ITSSA_2025.Models;

public partial class TaskingProgress
{
    public int Id { get; set; }

    public int? TaskId { get; set; }

    public int? UserId { get; set; }

    public DateOnly? Deadline { get; set; }

    public byte? IsSelesai { get; set; }

    public virtual Task? Task { get; set; }

    public virtual User? User { get; set; }
}
