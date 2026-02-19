using System;
using System.Collections.Generic;
using System.Text.Json.Serialization;

namespace LKS_ITSSA_2025.Models;

public partial class TodoTask
{
    public int Id { get; set; }
    [JsonIgnore]
    public int? TaskId { get; set; }

    public string? TodoTask1 { get; set; }

    public byte? IsSelesai { get; set; }

    public DateOnly? TanggalSelesai { get; set; }

    [JsonIgnore]
    public virtual Task? Task { get; set; }
}
